package io.wyrmise.jumpmanga.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import io.wyrmise.jumpmanga.adapters.DownloadedImageAdapter;
import io.wyrmise.jumpmanga.fragments.ChapterFragment;
import io.wyrmise.jumpmanga.utils.FileUtils;
import io.wyrmise.jumpmanga.widget.CustomViewPager;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.adapters.FullScreenImageAdapter;
import io.wyrmise.jumpmanga.animation.AnimationHelper;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.FetchingMachine;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.model.Page;
import io.wyrmise.jumpmanga.widget.TouchImageView;

public class ReadActivity extends AppCompatActivity {

    private JumpDatabaseHelper db;
    private FullScreenImageAdapter adapter;
    private ArrayList<Page> pages;
    private int increment = 0;
    private int chapter_position;
    private int chapter_position_temp;
    private Manga manga;
    private String name;
    private String img;
    private ArrayList<Chapter> chapters;
    Handler mHideHandler = new Handler();
    private AnimationHelper anim;
    private int calculatedPixel;
    private ProgressDialog progressDialog;
    private boolean isRefreshing = false;
    private MenuItem fv_button;

    @Bind(R.id.viewPager) CustomViewPager viewPager;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.seekBar) SeekBar seekBar;
    @Bind(R.id.progress) ProgressBar progressBar;
    @Bind(R.id.fullscreen_content_controls) View control;
    @Bind(R.id.indicator) TextView pageIndicator;
    @Bind(R.id.next) ImageView next;
    @Bind(R.id.previous) ImageView previous;


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

        ButterKnife.bind(this);

        db = new JumpDatabaseHelper(ReadActivity.this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        anim = new AnimationHelper(this);

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
//            @Override
//            public void onSwipeOutAtStart() {
//                prevChapter();
//            }

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

        calculatedPixel = convertToPx(20);

        progressDialog = new ProgressDialog(ReadActivity.this);
        progressDialog.setMessage("Loading chapter, please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextChapter();
                mHideHandler.removeCallbacks(hideControllerThread);
                autoHideControllers();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevChapter();
                mHideHandler.removeCallbacks(hideControllerThread);
                autoHideControllers();
            }
        });

        Intent intent = getIntent();

        manga = intent.getParcelableExtra("manga");
        name = manga.getName();
        img = manga.getImage();
        String url = intent.getStringExtra("url");
        String name = intent.getStringExtra("name");
        chapter_position = intent.getIntExtra("position", -1);
        chapters = intent.getParcelableArrayListExtra("list");

        setTitle(name);

        getSupportActionBar().setSubtitle(this.name);

        mHideHandler.postDelayed(hideControllerThread, 5000);

        getAppropriateChapter(url);

    }

    public void getAppropriateChapter(String url) {
        FileUtils fileUtils = new FileUtils();
        if (fileUtils.isChapterDownloaded(manga.getName(), chapters.get(chapter_position).getName())) {
            ArrayList<String> filePath = fileUtils.getFilePaths();
            if (filePath != null && filePath.size() > 0) {
                progressBar.setVisibility(View.INVISIBLE);
                DownloadedImageAdapter adapter = new DownloadedImageAdapter(ReadActivity.this, filePath,false);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(0);
                viewPager.setPageMargin(calculatedPixel);
                pageIndicator.setText("Page 1/" + adapter.getCount());
                seekBar.setProgress(0);
                seekBar.setMax(adapter.getCount() - 1);
                getFavoriteStatus();
                setRead();
            } else
                new RetrieveAllPages().execute(url);
        } else
            new RetrieveAllPages().execute(url);
    }

    public void changeDownloadedChapter() {
        FileUtils fileUtils = new FileUtils();
        if (fileUtils.isChapterDownloaded(manga.getName(), chapters.get(chapter_position).getName())) {
            ArrayList<String> filePath = fileUtils.getFilePaths();
            if (filePath != null && filePath.size() > 0) {
                progressBar.setVisibility(View.INVISIBLE);
                DownloadedImageAdapter adapter = new DownloadedImageAdapter(ReadActivity.this, filePath,false);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(0);
                viewPager.setPageMargin(calculatedPixel);
                pageIndicator.setText("Page 1/" + adapter.getCount());
                seekBar.setProgress(0);
                seekBar.setMax(adapter.getCount() - 1);
                getFavoriteStatus();
                setRead();
                getSupportActionBar().setTitle(chapters.get(chapter_position).getName());
            } else {
                ChangeChapter task = new ChangeChapter(progressDialog);
                task.execute(chapters.get(chapter_position).getUrl());
            }
        } else {
            ChangeChapter task = new ChangeChapter(progressDialog);
            task.execute(chapters.get(chapter_position).getUrl());
        }
    }


    public void nextChapter() {
        if ((chapter_position - 1) != -1) {
            chapter_position_temp = chapter_position;
            chapter_position--;
            changeDownloadedChapter();
        } else {
            Toast.makeText(getApplicationContext(), "This is the last chapter", Toast.LENGTH_SHORT).show();
        }
    }

    public void prevChapter() {
        if ((chapter_position + 1) != chapters.size()) {
            chapter_position_temp = chapter_position;
            chapter_position++;
            changeDownloadedChapter();
        } else {
            Toast.makeText(getApplicationContext(), "This is the first chapter", Toast.LENGTH_SHORT).show();
        }
    }

    public void setRead() {
        if (!chapters.get(chapter_position).isRead()) {
            db.markChapterAsRead(chapters.get(chapter_position), name);
            if (ChapterFragment.getAdapter() != null)
                ChapterFragment.getAdapter().getItem(chapter_position).setIsRead(true);
            else chapters.get(chapter_position).setIsRead(true);
        }
        db.insertRecentChapter(manga, chapters.get(chapter_position));
    }

    public void getFavoriteStatus() {
        System.out.println(fv_button==null);
        if(fv_button!=null) {
            if (!chapters.get(chapter_position).isFav()) {
                fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_unfav));
            } else
                fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_fav));
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_read, menu);
        fv_button = menu.findItem(R.id.favorite);
        getFavoriteStatus();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setFavorite() {
        if (db.isChapterFav(chapters.get(chapter_position), manga.getName())) {
            db.unfavChapter(chapters.get(chapter_position), manga.getName());
            if (ChapterFragment.getAdapter() != null)
                ChapterFragment.getAdapter().getItem(chapter_position).setIsFav(false);
            chapters.get(chapter_position).setIsFav(false);
            fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_unfav));
            Toast.makeText(ReadActivity.this, "Successfully unfavorited this chapter", Toast.LENGTH_SHORT).show();
        } else {
            db.favChapter(chapters.get(chapter_position), manga.getName());
            if (ChapterFragment.getAdapter() != null)
                ChapterFragment.getAdapter().getItem(chapter_position).setIsFav(true);
            chapters.get(chapter_position).setIsFav(true);
            fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_fav));
            Toast.makeText(ReadActivity.this, "Successfully favorited this chapter", Toast.LENGTH_SHORT).show();
        }
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
            case R.id.favorite:
                fv_button = item;
                setFavorite();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        ChangeChapter task = new ChangeChapter(progressDialog);
        isRefreshing = true;
        task.execute(chapters.get(chapter_position).getUrl());
    }

    public class RetrieveAllPages extends AsyncTask<String, Void, ArrayList<Page>> {
        public void onPreExecute() {
            next.setVisibility(ImageView.INVISIBLE);
            previous.setVisibility(ImageView.INVISIBLE);
        }

        public ArrayList<Page> doInBackground(String... params) {
            FetchingMachine download = new FetchingMachine(params[0]);
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
            next.setVisibility(ImageView.VISIBLE);
            previous.setVisibility(ImageView.VISIBLE);
            if (result != null) {
                pages = result;
                adapter = new FullScreenImageAdapter(ReadActivity.this, pages);
                viewPager.setAdapter(adapter);

                viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
                    boolean callHappened = false;

//                    @Override
//                    public void onSwipeOutAtStart() {
//                        if (!callHappened) {
//                            callHappened = true;
//                            prevChapter();
//                        }
//                    }

                    @Override
                    public void onSwipeOutAtEnd() {
                        if (!callHappened) {
                            callHappened = true;
                            nextChapter();
                        }
                    }
                });

                viewPager.setCurrentItem(0);
                viewPager.setOffscreenPageLimit(5);
                viewPager.setPageMargin(calculatedPixel);
                pageIndicator.setText("Page 1/" + adapter.getCount());
                progressBar.setProgress(0);
                increment = 0;
                progressBar.setMax(adapter.getCount());
                progressBar.setVisibility(View.VISIBLE);

                seekBar.setProgress(0);
                seekBar.setMax(adapter.getCount() - 1);

                getFavoriteStatus();

            } else {
                Toast.makeText(getApplicationContext(), "Failed to retrieve this chapter, please check your network", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    public class ChangeChapter extends AsyncTask<String, Void, ArrayList<Page>> {

        private ProgressDialog dialog;

        public ChangeChapter(ProgressDialog p) {
            dialog = p;
        }

        public void onPreExecute() {
            next.setVisibility(ImageView.INVISIBLE);
            previous.setVisibility(ImageView.INVISIBLE);
            showControllers();
            if (dialog != null)
                dialog.show();
        }

        public ArrayList<Page> doInBackground(String... params) {
            FetchingMachine download = new FetchingMachine(params[0]);
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
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (result != null) {
                pages = result;
                adapter = new FullScreenImageAdapter(ReadActivity.this, pages);
                viewPager.setAdapter(adapter);
                viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {

                    boolean callHappened = false;

                    @Override
                    public void onSwipeOutAtEnd() {
                        if (!callHappened) {
                            callHappened = true;
                            nextChapter();
                        }
                    }
                });

                viewPager.setCurrentItem(0);
                viewPager.setOffscreenPageLimit(5);
                viewPager.setPageMargin(calculatedPixel);
                pageIndicator.setText("Page 1/" + adapter.getCount());
                progressBar.setProgress(0);
                increment = 0;
                progressBar.setMax(adapter.getCount());
                progressBar.setVisibility(View.VISIBLE);

                seekBar.setProgress(0);
                seekBar.setMax(adapter.getCount() - 1);

                isRefreshing = false;

                getFavoriteStatus();
                setRead();
                getSupportActionBar().setTitle(chapters.get(chapter_position).getName());

                mHideHandler.postDelayed(hideControllerThread, 2000);
            } else {
                Toast.makeText(getApplicationContext(), "Cannot retrieve new chapter, please check your network", Toast.LENGTH_SHORT).show();
                if (!isRefreshing)
                    chapter_position = chapter_position_temp;
                viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {

                    boolean callHappened = false;

                    @Override
                    public void onSwipeOutAtEnd() {
                        if (!callHappened) {
                            callHappened = true;
                            nextChapter();
                        }
                    }
                });
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
