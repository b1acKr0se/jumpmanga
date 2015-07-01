package io.wyrmise.jumpmanga;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;

public class ReaderActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;

    private ViewPager viewPager;

    private ArrayList<Page> pages;

    private Toolbar toolbar;

    private View control;

    private TextView pageIndicator;

    Handler mHideHandler = new Handler();

    private Runnable hideControllerThread = new Runnable() {
        public void run() {
            slideOutFromBottom(control);
            slideOutFromTop(toolbar);
        }
    };

    public void autoHideControllers() {
        mHideHandler.postDelayed(hideControllerThread, 3000);
    }

    public void showControllers() {
        slideInFromBottom(control);
        slideInFromTop(toolbar);
        mHideHandler.removeCallbacks(hideControllerThread);
        autoHideControllers();
    }

    public void onClick(View v) {
        if (control.getVisibility() == View.VISIBLE) {
            mHideHandler.removeCallbacks(hideControllerThread);
            hideControllers();
        } else {
            showControllers();
        }
    }

    public void hideControllers(){
        slideOutFromBottom(control);
        slideOutFromTop(toolbar);
    }

    public void slideInFromBottom(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
            view.startAnimation(in);
            view.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void slideOutFromBottom(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
            view.startAnimation(in);
            view.setVisibility(View.GONE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void slideInFromTop(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_top);
            view.startAnimation(in);
            view.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void slideOutFromTop(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_top);
            view.startAnimation(in);
            view.setVisibility(View.GONE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        pageIndicator = (TextView) findViewById(R.id.indicator);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pageIndicator.setText("Page "+(position+1)+"/"+viewPager.getAdapter().getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        control = findViewById(R.id.fullscreen_content_controls);


        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");

        setTitle(name);

        setSupportActionBar(toolbar);

        new RetrieveAllPages().execute(url);
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
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class RetrieveAllPages extends AsyncTask<String, Void, ArrayList<Page>> {
        public ArrayList<Page> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Page> arr;
            try {
                arr = download.GetPages();
                return arr;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(ArrayList<Page> result) {
            if (result != null) {
                pages = result;
                adapter = new FullScreenImageAdapter(ReaderActivity.this, pages);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(0);
                pageIndicator.setText("Page 1/"+adapter.getCount());
            }
        }
    }
}
