package io.wyrmise.jumpmanga.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.adapters.DownloadedImageAdapter;
import io.wyrmise.jumpmanga.adapters.FullScreenImageAdapter;
import io.wyrmise.jumpmanga.animation.AnimationHelper;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.widget.CustomViewPager;
import io.wyrmise.jumpmanga.widget.TouchImageView;

public class DownloadedReadActivity extends AppCompatActivity {

    private JumpDatabaseHelper db;

    private FullScreenImageAdapter adapter;

    private CustomViewPager viewPager;

    private Toolbar toolbar;

    private View control;

    private TextView pageIndicator;

    private SeekBar seekBar;

    private ImageView next, previous;

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
        }
    };

    public void autoHideControllers() {
        mHideHandler.postDelayed(hideControllerThread, 15000);
    }

    public void showControllers() {
        showSystemUI();
        anim.slideInFromBottom(control);
        anim.slideInFromTop(toolbar);
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_read);

        db = new JumpDatabaseHelper(DownloadedReadActivity.this);

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

        calculatedPixel = convertToPx(20);

        control = findViewById(R.id.fullscreen_content_controls);

        Intent intent = getIntent();

        String chapter_name = intent.getStringExtra("chapter_name");

        ArrayList<String> path = intent.getStringArrayListExtra("image_path");

        if(path!=null) {
            DownloadedImageAdapter adapter = new DownloadedImageAdapter(DownloadedReadActivity.this, path,true);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(0);
            viewPager.setPageMargin(calculatedPixel);
            pageIndicator.setText("Page 1/" + adapter.getCount());
            seekBar.setProgress(0);
            seekBar.setMax(adapter.getCount() - 1);
        }

        setTitle(chapter_name);

        setSupportActionBar(toolbar);

        mHideHandler.postDelayed(hideControllerThread, 3000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
}
