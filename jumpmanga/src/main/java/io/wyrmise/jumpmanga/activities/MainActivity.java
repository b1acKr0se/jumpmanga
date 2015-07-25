/*
 * Copyright (C) 2015 Hai Nguyen Thanh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.wyrmise.jumpmanga.activities;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;
import io.wyrmise.jumpmanga.fragments.CategoryFragment;
import io.wyrmise.jumpmanga.fragments.DownloadedFragment;
import io.wyrmise.jumpmanga.widget.CustomAutoCompleteTextView;
import io.wyrmise.jumpmanga.fragments.MainFragment;
import io.wyrmise.jumpmanga.fragments.NewFragment;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.fragments.RecentFragment;
import io.wyrmise.jumpmanga.fragments.SubscriptionFragment;
import io.wyrmise.jumpmanga.adapters.SearchAdapter;
import io.wyrmise.jumpmanga.adapters.SpinnerAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.model.Category;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.picasso.CircleTransform;
import io.wyrmise.jumpmanga.service.FetchLatestService;

public class MainActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener {

    public static final String AVATAR_URL = "http://lorempixel.com/200/200/people/1/";
    private ArrayList<Manga> mangas;
    private ArrayList<Manga> temp;
    private ArrayList<Category> categories;
    private int savedMenuId = -1;
    private int category_position = -1;
    private JumpDatabaseHelper db;
    private SearchAdapter adapter;
    private MenuItem toggle_btn;
    private Spinner spinner;
    private SpinnerAdapter spinnerAdapter;
    private View spinnerContainer;
    private boolean isManuallySelected = true;
    private SharedPreferences prefs;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.search_box) CustomAutoCompleteTextView searchBox;
    @Bind(R.id.avatar) ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        db = new JumpDatabaseHelper(MainActivity.this);

        initToolbar();
        setupDrawerLayout();

        categories = db.getAllCategories();

        prefs = getSharedPreferences("Alarm", MODE_PRIVATE);

        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                toggleSearch(true);
                int position = temp.indexOf(adapter.getItem(i));
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("manga", temp.get(position));
                startActivity(intent);
            }
        });

        searchBox.setOnClearListener(new CustomAutoCompleteTextView.OnClearListener() {

            @Override
            public void onClear() {
                toggleSearch(true);
            }
        });


        Picasso.with(this).load(AVATAR_URL).transform(new CircleTransform()).into(avatar);

        boolean isOpenFromNotification = getIntent().getBooleanExtra("favorite", false);

        if (savedInstanceState == null && !isOpenFromNotification) {
            GetHotMangas();
            new getAllMangas().execute();

        } else if (isOpenFromNotification) {
            GetSubscription();
            new getAllMangas().execute();
            NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
            Menu menu = view.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == R.id.drawer_feeds) {
                    savedMenuId = item.getItemId();
                    item.setChecked(true);
                }
            }
        } else if (savedInstanceState != null) {
            getSupportActionBar().setTitle(savedInstanceState.getString("title"));
            mangas = savedInstanceState.getParcelableArrayList("list");
            temp = new ArrayList<>(mangas);
            adapter = new SearchAdapter(getApplicationContext(), R.layout.search_dropdown_item, mangas);
            searchBox.setAdapter(adapter);
            NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
            Menu menu = view.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == savedInstanceState.getInt("menu")) {
                    savedMenuId = savedInstanceState.getInt("menu");
                    item.setChecked(true);
                }
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        boolean isOpenFromNotification = intent.getBooleanExtra("favorite", false);
        if (isOpenFromNotification) {
            GetSubscription();
            new getAllMangas().execute();
            NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
            Menu menu = view.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == R.id.drawer_feeds) {
                    savedMenuId = item.getItemId();
                    item.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!prefs.getBoolean("Alarm", false)) {
            Intent i = new Intent(this, FetchLatestService.class);
            PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.cancel(pi);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 480 * 60 * 1000,
                    480 * 60 * 1000, pi);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Alarm", true);
            editor.commit();
        }
    }

    protected void toggleSearch(boolean reset) {
        if (reset) {
            // hide search box and show search icon
            searchBox.setText("");
            searchBox.setVisibility(View.GONE);
            toggle_btn.setVisible(true);
            // hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
        } else {
            // hide search icon and show search box
            toggle_btn.setVisible(false);
            searchBox.setVisibility(View.VISIBLE);
            searchBox.requestFocus();
            // show the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("title", getSupportActionBar().getTitle().toString());
        bundle.putInt("menu", savedMenuId);
        bundle.putParcelableArrayList("list", mangas);
        bundle.putInt("category", category_position);
        super.onSaveInstanceState(bundle);
    }


    private void GetNewMangas() {
        NewFragment fragment = new NewFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "NEW");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("New");
    }


    private void GetHotMangas() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("manga_type", 1);
        fragment.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "HOT");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("Jump Manga");
    }

    private void GetFavoriteMangas() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("manga_type", 2);
        fragment.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "FAVOURITE");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("Favourite");
    }

    private void GetRecentList() {
        RecentFragment fragment = new RecentFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "RECENT");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("Recent");
    }

    private void GetCategories(Category c) {
        if (spinnerContainer == null)
            setUpSpinner();
        CategoryFragment fragment = new CategoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", c.getUrl());
        bundle.putInt("max_page", c.getPage());
        bundle.putInt("position", categories.indexOf(c));
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "CATEGORY");
        fragmentTransaction.commit();
    }

    private void GetSubscription() {
        SubscriptionFragment fragment = new SubscriptionFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "FEEDS");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("My feeds");
    }

    public void GetDownloaded() {
        DownloadedFragment fragment = new DownloadedFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "DOWNLOADED");
        fragmentTransaction.commit();
        removeSpinner();
        getSupportActionBar().setTitle("Downloaded");
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        toggle_btn = menu.findItem(R.id.toggle_button);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawers();
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.toggle_button:
                toggleSearch(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
    }

    public void setUpSpinner() {
        spinnerContainer = LayoutInflater.from(this).inflate(R.layout.spinner,
                toolbar, false);

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        spinnerAdapter = new SpinnerAdapter(MainActivity.this);

        if (categories != null) {
            spinnerAdapter.addItems(categories);
            spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
            spinner.setOnItemSelectedListener(MainActivity.this);
            spinner.setAdapter(spinnerAdapter);
        }

    }

    public void setSpinnerPosition(int position) {
        if (spinner != null) {
            isManuallySelected = false;
            spinner.setSelection(position);
        }
    }

    private void removeSpinner() {
        if (spinnerContainer != null) {
            toolbar.removeView(spinnerContainer);
            spinnerContainer = null;
        }
    }

    private void setupDrawerLayout() {

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                savedMenuId = menuItem.getItemId();
                switch (menuItem.getItemId()) {
                    case R.id.drawer_home:
                        GetHotMangas();
                        break;
                    case R.id.drawer_favourite:
                        GetFavoriteMangas();
                        break;
                    case R.id.drawer_recent:
                        GetRecentList();
                        break;
                    case R.id.drawer_new:
                        GetNewMangas();
                        break;
                    case R.id.drawer_category:
                        GetCategories(categories.get(0));
                        break;
                    case R.id.drawer_feeds:
                        GetSubscription();
                        break;
                    case R.id.drawer_downloaded:
                        GetDownloaded();
                        break;
                    case R.id.drawer_settings:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                startActivity(intent);
                            }
                        },200);
                        break;
                }
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (isManuallySelected) {
            Category c = (Category) spinnerAdapter.getItem(i);
            GetCategories(c);
        } else {
            isManuallySelected = true;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class getAllMangas extends AsyncTask<Void, Void, ArrayList<Manga>> {

        @Override
        public void onPreExecute() {
        }

        @Override
        public ArrayList<Manga> doInBackground(Void... params) {
            return db.getAllMangas();
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {

            if (result != null) {
                mangas = result;
                temp = new ArrayList<>(mangas);
                adapter = new SearchAdapter(getApplicationContext(), R.layout.search_dropdown_item, mangas);
                searchBox.setAdapter(adapter);
            } else {
                searchBox.setVisibility(View.GONE);
            }
        }
    }






}
