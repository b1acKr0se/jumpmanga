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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.animation.AnimationHelper;
import io.wyrmise.jumpmanga.database.DatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.model.Page;
import io.wyrmise.jumpmanga.widget.TouchImageView;

public class ReadActivity extends AppCompatActivity {

    private DatabaseHelper db;

    private FullScreenImageAdapter adapter;

    private CustomViewPager viewPager;

    private ArrayList<Page> pages;

    private Toolbar toolbar;

    private View control;

    private TextView pageIndicator;

    private ProgressBar progressBar;

    private SeekBar seekBar;

    private ImageView next, previous;

    private int increment = 0;

    private int chapter_position;

    private Manga manga;

    private String name;

    private String img;

    private ArrayList<Chapter> chapters;

    Handler mHideHandler = new Handler();

    private AnimationHelper anim;

    private int calculatedPixel;

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
            anim.slideOutFromBottom(control);
            anim.slideOutFromTop(toolbar);
            if (progressBar.getVisibility() != ProgressBar.INVISIBLE)
                anim.slideOutFromTop(progressBar);
        }
    };

    public void autoHideControllers() {
        mHideHandler.postDelayed(hideControllerThread, 15000);
    }

    public void showControllers() {
        showSystemUI();
        anim.slideInFromBottom(control);
        anim.slideInFromTop(toolbar);
        if (progressBar.getVisibility() != ProgressBar.INVISIBLE)
            anim.slideInFromTop(progressBar);
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
        anim.slideOutFromBottom(control);
        anim.slideOutFromTop(toolbar);
        if (progressBar.getVisibility() != ProgressBar.INVISIBLE)
            anim.slideOutFromTop(progressBar);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        setContentView(R.layout.activity_read);

        db = new DatabaseHelper(ReadActivity.this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        anim = new AnimationHelper(this);

        viewPager = (CustomViewPager) findViewById(R.id.viewPager);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageIndicator.setText("Page " + (position + 1) + "/" + viewPager.getAdapter().getCount());

                seekBar.setProgress(position);

                if (position > 0) {
                    View view = viewPager.findViewWithTag(position - 1);
                    if (view != null) {
                        TouchImageView img = (TouchImageView) view.findViewById(R.id.img);
                        img.resetZoom();
                    }
                }
                if (position < viewPager.getAdapter().getCount() - 1) {
                    View view = viewPager.findViewWithTag(position + 1);
                    if (view != null) {
                        TouchImageView img = (TouchImageView) view.findViewById(R.id.img);
                        img.resetZoom();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                prevChapter();
            }

            @Override
            public void onSwipeOutAtEnd() {
                nextChapter();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                viewPager.setCurrentItem(i, true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pageIndicator = (TextView) findViewById(R.id.indicator);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        calculatedPixel = convertToPx(20);

        next = (ImageView) findViewById(R.id.next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextChapter();
                mHideHandler.removeCallbacks(hideControllerThread);
                autoHideControllers();
            }
        });

        previous = (ImageView) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevChapter();
                mHideHandler.removeCallbacks(hideControllerThread);
                autoHideControllers();
            }
        });


        control = findViewById(R.id.fullscreen_content_controls);

        Intent intent = getIntent();

        manga = intent.getParcelableExtra("manga");
        name = manga.getName();
        img = manga.getImage();
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        chapter_position = intent.getIntExtra("position", -1);
        chapters = intent.getParcelableArrayListExtra("list");

        setTitle(name);

        setSupportActionBar(toolbar);

        mHideHandler.postDelayed(hideControllerThread, 3000);

        new RetrieveAllPages().execute(url);
    }

    public void nextChapter() {
        if ((chapter_position - 1) != -1) {
            chapter_position--;
            if (!chapters.get(chapter_position).isRead()) {
                db.insertChapter(chapters.get(chapter_position), name);
                if (ChapterFragment.getAdapter() != null)
                    ChapterFragment.getAdapter().getItem(chapter_position).setIsRead(true);
                else chapters.get(chapter_position).setIsRead(true);
            }
            db.insertRecentChapter(manga, chapters.get(chapter_position));
            new RetrieveAllPages().execute(chapters.get(chapter_position).getUrl());
            getSupportActionBar().setTitle(chapters.get(chapter_position).getName());
        } else {
            Toast.makeText(getApplicationContext(), "This is the last chapter", Toast.LENGTH_SHORT).show();
        }
    }

    public void prevChapter() {
        if ((chapter_position + 1) != chapters.size()) {
            chapter_position++;
            if (!chapters.get(chapter_position).isRead()) {
                db.insertChapter(chapters.get(chapter_position), name);
                if (ChapterFragment.getAdapter() != null)
                    ChapterFragment.getAdapter().getItem(chapter_position).setIsRead(true);
                else chapters.get(chapter_position).setIsRead(true);
            }
            db.insertRecentChapter(manga, chapters.get(chapter_position));
            new RetrieveAllPages().execute(chapters.get(chapter_position).getUrl());
            getSupportActionBar().setTitle(chapters.get(chapter_position).getName());
        } else {
            Toast.makeText(getApplicationContext(), "This is the first chapter", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProgress() {
        increment++;
        progressBar.setProgress(increment);
        if (progressBar.getProgress() == progressBar.getMax())
            progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        viewPager.setAdapter(adapter);
    }

    public class RetrieveAllPages extends AsyncTask<String, Void, ArrayList<Page>> {
        public void onPreExecute() {
            next.setVisibility(ImageView.INVISIBLE);
            previous.setVisibility(ImageView.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Loading new chapter, please wait", Toast.LENGTH_SHORT).show();
        }

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
                adapter = new FullScreenImageAdapter(ReadActivity.this, pages);
                viewPager.setAdapter(adapter);

                viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
                    boolean callHappened = false;

                    @Override
                    public void onSwipeOutAtStart() {
                        if (!callHappened) {
                            callHappened = true;
                            prevChapter();
                        }
                    }

                    @Override
                    public void onSwipeOutAtEnd() {
                        if (!callHappened) {
                            callHappened = true;
                            nextChapter();
                        }
                    }
                });

                viewPager.setCurrentItem(0);
                viewPager.setOffscreenPageLimit(3);
                viewPager.setPageMargin(calculatedPixel);
                pageIndicator.setText("Page 1/" + adapter.getCount());
                progressBar.setProgress(0);
                increment = 0;
                progressBar.setMax(adapter.getCount());
                progressBar.setVisibility(View.VISIBLE);

                seekBar.setProgress(0);
                seekBar.setMax(adapter.getCount() - 1);

            }
            next.setVisibility(ImageView.VISIBLE);
            previous.setVisibility(ImageView.VISIBLE);
        }
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}