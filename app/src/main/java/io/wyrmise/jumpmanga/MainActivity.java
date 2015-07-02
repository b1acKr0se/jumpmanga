package io.wyrmise.jumpmanga;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.picasso.CircleTransform;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;

public class MainActivity extends AppCompatActivity implements MangaAdapter.OnItemClickListener {

    public static final String AVATAR_URL = "http://lorempixel.com/200/200/people/1/";

    private ArrayList<Manga> mangas = new ArrayList<>();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private View content;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        setupDrawerLayout();

        content = findViewById(R.id.content);

        final ImageView avatar = (ImageView) findViewById(R.id.avatar);

        Picasso.with(this).load(AVATAR_URL).transform(new CircleTransform()).into(avatar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);


        new GetMangas().execute("http://manga24h.com/status/hot.html");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
                Snackbar.make(content, menuItem.getTitle() + " pressed", Snackbar.LENGTH_LONG).show();
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
    public void onItemClick(View view, Manga manga) {
        Intent intent = new Intent(getApplicationContext(), DetailedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("title", manga.getName());
        intent.putExtra("url",manga.getUrl());
        intent.putExtra("image", manga.getImage());
        startActivity(intent);
    }


    public class GetMangas extends AsyncTask<String, Void, ArrayList<Manga>> {

        @Override
        public void onPreExecute() {

        }

        @Override
        public ArrayList<Manga> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            return download.GetMangas(10);
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            if (result != null) {
                mangas = result;
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                MangaAdapter adapter = new MangaAdapter(mangas);
                adapter.setOnItemClickListener(MainActivity.this);
                recyclerView.setAdapter(adapter);
            }
        }
    }

}
