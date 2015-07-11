package io.wyrmise.jumpmanga;

import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.widget.SlidingTabLayout;

public class DetailActivity extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getManga().getName());

        pager = (ViewPager) findViewById(R.id.pager);

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),DetailActivity.this);

        pager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        tabLayout.setTabTextColors(getResources().getColor(R.color.green_dark),getResources().getColor(android.R.color.white));

        tabLayout.setupWithViewPager(pager);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

//    @Override
//    public void onSaveInstanceState(Bundle bundle) {
//        super.onSaveInstanceState(bundle);
//    }

    public Manga getManga() {
        return getIntent().getParcelableExtra("manga");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
