package io.demiseq.jetreader.activities;

import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.adapters.ViewPagerAdapter;
import io.demiseq.jetreader.model.Manga;

public class DetailActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.pager) ViewPager pager;
    @Bind(R.id.sliding_tabs) TabLayout tabLayout;

    @BindString(R.string.info) String info;
    @BindString(R.string.chapter) String chapter;


    ViewPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getManga().getName());

        String[] titles = {info,chapter};

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),DetailActivity.this, titles);

        pager.setAdapter(adapter);

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