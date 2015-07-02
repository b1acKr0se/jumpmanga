package io.wyrmise.jumpmanga;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Page;

public class ReaderActivity extends AppCompatActivity {

    private FullScreenImageAdapter adapter;

    private ViewPager viewPager;

    private ArrayList<Page> pages;

    private Toolbar toolbar;

    private View control;

    private TextView pageIndicator;

    private ProgressBar progressBar;

    private ImageView next, previous;

    int increment = 0;

    int chapter_position;

    private ArrayList<Chapter> chapters;

    Handler mHideHandler = new Handler();

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private Runnable hideControllerThread = new Runnable() {
        public void run() {
            hideSystemUI();
            slideOutFromBottom(control);
            slideOutFromTop(toolbar);
        }
    };

    public void autoHideControllers() {
        mHideHandler.postDelayed(hideControllerThread, 15000);
    }

    public void showControllers() {
        showSystemUI();
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

    public void hideControllers() {
        hideSystemUI();
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

        progressBar = (ProgressBar) findViewById(R.id.progress);

        next = (ImageView) findViewById(R.id.next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextChapter();
            }
        });

        previous = (ImageView) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevChapter();
            }
        });




        control = findViewById(R.id.fullscreen_content_controls);


        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        chapter_position = intent.getIntExtra("position",-1);
        chapters = intent.getParcelableArrayListExtra("list");

        setTitle(name);

        setSupportActionBar(toolbar);

        mHideHandler.postDelayed(hideControllerThread, 3000);

        new RetrieveAllPages().execute(url);
    }

    public void nextChapter(){
        if(chapters.get(chapter_position-1)!=null){
            chapter_position--;
            new RetrieveAllPages().execute(chapters.get(chapter_position).getUrl());
            getSupportActionBar().setTitle(chapters.get(chapter_position).getName());
        } else {
            Toast.makeText(getApplicationContext(),"No next chapter",Toast.LENGTH_LONG).show();
        }
    }

    public void prevChapter(){
        if(chapters.get(chapter_position+1)!=null){
            chapter_position++;
            new RetrieveAllPages().execute(chapters.get(chapter_position).getUrl());
            getSupportActionBar().setTitle(chapters.get(chapter_position).getName());
        } else {
            Toast.makeText(getApplicationContext(),"No previous chapter",Toast.LENGTH_LONG).show();
        }
    }

    public void updateProgress() {
        increment++;
        progressBar.setProgress(increment);
        if(progressBar.getProgress()==progressBar.getMax())
            progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
                pageIndicator.setText("Page 1/" + adapter.getCount());
                progressBar.setProgress(0);
                increment = 0;
                progressBar.setMax(adapter.getCount());
                progressBar.setVisibility(View.VISIBLE);

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    boolean firstPageChange = false;
                    boolean lastPageChange = false;
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        int lastPosition = adapter.getCount()-1;
                        if(firstPageChange && position==0){
                            System.out.println("First page");
                        } else if(lastPageChange && position==lastPosition){
                            System.out.println("Last page");
                        }
                    }

                    @Override
                    public void onPageSelected(int position) {
                        pageIndicator.setText("Page " + (position + 1) + "/" + viewPager.getAdapter().getCount());
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        int lastPosition = adapter.getCount()-1;

                        int currentPosition = viewPager.getCurrentItem();

                        if(currentPosition==lastPosition && state==1) {
                            lastPageChange = true;
                            firstPageChange = false;
                        } else if(currentPosition==0 && state==1) {
                            lastPageChange = false;
                            firstPageChange = true;
                        } else {
                            lastPageChange = false;
                            firstPageChange = false;
                        }
                    }
                });
            }
        }
    }
}
