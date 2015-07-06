package io.wyrmise.jumpmanga;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.wyrmise.jumpmanga.picasso.CircleTransform;

public class MainActivity extends AppCompatActivity {

    public static final String AVATAR_URL = "http://lorempixel.com/200/200/people/1/";

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private View content;

    private int savedMenuId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_main);

        initToolbar();
        setupDrawerLayout();

        content = findViewById(R.id.content);

        final ImageView avatar = (ImageView) findViewById(R.id.avatar);

        Picasso.with(this).load(AVATAR_URL).transform(new CircleTransform()).into(avatar);

        if(savedInstanceState==null)
            GetHotMangas();
        else {
            getSupportActionBar().setTitle(savedInstanceState.getString("title"));
            NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
            Menu menu = view.getMenu();
            for(int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if(item.getItemId()==savedInstanceState.getInt("menu"))
                    item.setChecked(true);
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("title",getSupportActionBar().getTitle().toString());
        bundle.putInt("menu",savedMenuId);
        super.onSaveInstanceState(bundle);
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
        getSupportActionBar().setTitle("Favourite");
    }

    private void GetRecentList() {
        RecentFragment fragment = new RecentFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, "RECENT");
        fragmentTransaction.commit();
        getSupportActionBar().setTitle("Recent");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawers();
        else super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

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


}
