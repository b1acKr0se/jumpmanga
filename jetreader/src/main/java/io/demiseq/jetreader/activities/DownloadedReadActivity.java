package io.demiseq.jetreader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.adapters.DownloadedImageAdapter;
import io.demiseq.jetreader.animation.AnimationHelper;
import io.demiseq.jetreader.database.JumpDatabaseHelper;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.utils.ChapterNameComparator;
import io.demiseq.jetreader.utils.FileUtils;
import io.demiseq.jetreader.widget.CustomViewPager;
import io.demiseq.jetreader.widget.TouchImageView;

public class DownloadedReadActivity extends BaseActivity {

    private ArrayList<Chapter> chapters;

    private JumpDatabaseHelper db;
    private Handler mHideHandler = new Handler();
    private AnimationHelper anim;
    private FileUtils fileUtils = new FileUtils();

    private int position = -1;
    private int calculatedPixel;

    @Bind(R.id.viewPager)CustomViewPager viewPager;
    @Bind(R.id.toolbar)Toolbar toolbar;
    @Bind(R.id.seekBar)SeekBar seekBar;
    @Bind(R.id.fullscreen_content_controls)View control;
    @Bind(R.id.indicator)TextView pageIndicator;

    @BindString(R.string.first_chapter) String first_chapter;
    @BindString(R.string.last_chapter) String last_chapter;

    @OnClick(R.id.next)
    public void next(){
        nextChapter();
    }

    @OnClick(R.id.previous)
    public void prev(){
        previousChapter();
    }

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

        ButterKnife.bind(this);

        db = new JumpDatabaseHelper(DownloadedReadActivity.this);

        fileUtils = new FileUtils();

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        anim = new AnimationHelper(this);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageIndicator.setText((position + 1) + "/" + viewPager.getAdapter().getCount());

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

        Intent intent = getIntent();

        String manga_name = intent.getStringExtra("manga_name");

        String chapter_name = intent.getStringExtra("chapter_name");

        ArrayList<String> path = intent.getStringArrayListExtra("image_path");

        Chapter c = new Chapter();
        c.setMangaName(manga_name);
        c.setName(chapter_name);
        c.setPath(path);

        chapters = getAllDownloadedChapter(manga_name);

        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).getName().equals(chapter_name))
                position = i;
        }

        if (path != null) {
            setUpAdapter(chapter_name, path);
        }

        db.markChapterAsRead(c,manga_name);

        getSupportActionBar().setSubtitle(manga_name);

        mHideHandler.postDelayed(hideControllerThread, 3000);

    }

    private ArrayList<Chapter> getAllDownloadedChapter(String mangaName) {
        ArrayList<Chapter> list = new ArrayList<>();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/.Jump Manga/" + mangaName);
        if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
            File[] chapter = dir.listFiles();
            for (int i = 0; i < chapter.length; i++) {
                if (chapter[i].isDirectory() && chapter[i].listFiles().length > 0) {
                    Chapter c = new Chapter();
                    c.setMangaName(mangaName);
                    c.setName(chapter[i].getName());
                    if (fileUtils.isChapterDownloaded(mangaName, c.getName())) {
                        c.setPath(fileUtils.getFilePaths());
                        list.add(c);
                    }
                }
            }
        }
        Collections.sort(list, new ChapterNameComparator());
        return list;
    }

    private void nextChapter() {
        if (position - 1 >= 0) {
            position--;
            Chapter c = chapters.get(position);
            String chapterName = c.getName();
            ArrayList<String> path = c.getPath();
            db.markChapterAsRead(c,c.getMangaName());
            setUpAdapter(chapterName, path);
        } else
            Toast.makeText(DownloadedReadActivity.this,last_chapter,Toast.LENGTH_SHORT).show();
    }

    private void previousChapter() {
        if (position + 1 != chapters.size()) {
            position++;
            Chapter c = chapters.get(position);
            String chapterName = c.getName();
            ArrayList<String> path = c.getPath();
            db.markChapterAsRead(c,c.getMangaName());
            setUpAdapter(chapterName, path);
        } else
            Toast.makeText(DownloadedReadActivity.this,first_chapter,Toast.LENGTH_SHORT).show();
    }

    private void setUpAdapter(String chapterName, ArrayList<String> path) {
        setTitle(chapterName);
        DownloadedImageAdapter adapter = new DownloadedImageAdapter(DownloadedReadActivity.this, path, true);
        viewPager.setAdapter(adapter);
        viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
            boolean callHappened = false;

            @Override
            public void onSwipeOutAtEnd() {
                if (!callHappened) {
                    callHappened = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextChapter();
                        }
                    }, 500);
                }
            }
        });
        viewPager.setCurrentItem(0);
        viewPager.setPageMargin(calculatedPixel);
        pageIndicator.setText("1/" + adapter.getCount());
        seekBar.setProgress(0);
        seekBar.setMax(adapter.getCount() - 1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

}
