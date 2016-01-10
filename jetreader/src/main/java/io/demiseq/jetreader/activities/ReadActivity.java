package io.demiseq.jetreader.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import butterknife.BindString;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.adapters.DownloadedImageAdapter;
import io.demiseq.jetreader.adapters.FullScreenImageAdapter;
import io.demiseq.jetreader.animation.AnimationHelper;
import io.demiseq.jetreader.api.MangaLibrary;
import io.demiseq.jetreader.database.JumpDatabaseHelper;
import io.demiseq.jetreader.fragments.ChapterFragment;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.model.Manga;
import io.demiseq.jetreader.model.Page;
import io.demiseq.jetreader.utils.FileUtils;
import io.demiseq.jetreader.widget.CustomViewPager;
import io.demiseq.jetreader.widget.TouchImageView;

public class ReadActivity extends BaseActivity {

    private Manga manga;
    private String name;
    private ArrayList<Chapter> chapters;

    private boolean isRefreshing = false;
    private boolean isControllerShowing = true;

    private int chapter_position;
    private int chapter_position_temp;
    private int calculatedPixel;
    private int currentPage = 0;
    private int increment = 0;

    private JumpDatabaseHelper db;
    private AnimationHelper anim;
    private ProgressDialog progressDialog;
    private MenuItem fv_button;

    private Handler mHideHandler = new Handler();

    @Bind(R.id.viewPager) CustomViewPager viewPager;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.seekBar) SeekBar seekBar;
    @Bind(R.id.progress) ProgressBar progressBar;
    @Bind(R.id.fullscreen_content_controls) View control;
    @Bind(R.id.indicator) TextView pageIndicator;
    @Bind(R.id.next) TextView next;
    @Bind(R.id.previous) TextView previous;

    @BindString(R.string.first_chapter) String first_chapter;
    @BindString(R.string.last_chapter) String last_chapter;
    @BindString(R.string.network_error) String network_error;

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
            hideControllers();
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
        isControllerShowing = true;
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
        isControllerShowing = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
                mHideHandler.removeCallbacks(hideControllerThread);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoHideControllers();
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
                if (isControllerShowing)
                    autoHideControllers();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevChapter();
                mHideHandler.removeCallbacks(hideControllerThread);
                if (isControllerShowing)
                    autoHideControllers();
            }
        });

        Intent intent = getIntent();

        manga = intent.getParcelableExtra("manga");
        name = manga.getName();

        String name = intent.getStringExtra("name");
        chapters = intent.getParcelableArrayListExtra("list");

//        if(savedInstanceState == null) {
        chapter_position = intent.getIntExtra("position", -1);
//        } else {
//            chapter_position = savedInstanceState.getInt("position");
//            currentPage = savedInstanceState.getInt("page_num");
//            url = chapters.get(chapter_position).getUrl();
//            Toast.makeText(this, "restoreInstancestate at chapter " + chapter_position, Toast.LENGTH_LONG).show();
//        }

        setTitle(name);

        getSupportActionBar().setSubtitle(this.name);

        getChapter(true);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("position", chapter_position);
        outState.putInt("page_num", currentPage);
        Toast.makeText(this, "savedinstancestate at chapter " + chapter_position, Toast.LENGTH_LONG).show();
    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onRestoreInstanceState(savedInstanceState, persistentState);
//        chapter_position = savedInstanceState.getInt("position");
//        currentPage = savedInstanceState.getInt("page_num");
//        String url = chapters.get(chapter_position).getUrl();
//        getAppropriateChapter(url);
//    }


    public void getChapter(boolean isFromList) {
        FileUtils fileUtils = new FileUtils();
        if (fileUtils.isChapterDownloaded(manga.getName(), chapters.get(chapter_position).getName())) {
            ArrayList<String> filePath = fileUtils.getFilePaths();
            if (filePath != null && filePath.size() > 0) {
                progressBar.setVisibility(View.INVISIBLE);
                DownloadedImageAdapter adapter = new DownloadedImageAdapter(ReadActivity.this, filePath,false);
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
                getFavoriteStatus();
                setRead();
                setTitle(chapters.get(chapter_position).getName());
            } else {
                LoadChapterTask task = new LoadChapterTask(isFromList, progressDialog);
                task.execute(chapters.get(chapter_position).getUrl());
            }
        } else {
            LoadChapterTask task = new LoadChapterTask(isFromList, progressDialog);
            task.execute(chapters.get(chapter_position).getUrl());
        }
    }


    public void nextChapter() {
        currentPage = 0;
        if ((chapter_position - 1) != -1) {
            chapter_position_temp = chapter_position;
            chapter_position--;
            getChapter(false);
        } else {
            Toast.makeText(getApplicationContext(), last_chapter, Toast.LENGTH_SHORT).show();
        }
    }

    public void prevChapter() {
        currentPage = 0;
        if ((chapter_position + 1) != chapters.size()) {
            chapter_position_temp = chapter_position;
            chapter_position++;
            getChapter(false);
        } else {
            Toast.makeText(getApplicationContext(), first_chapter, Toast.LENGTH_SHORT).show();
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
        System.out.println(fv_button == null);
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
        if(!isControllerShowing) hideSystemUI();
    }

    private void setFavorite() {
        if (db.isChapterFav(chapters.get(chapter_position), manga.getName())) {
            db.unfavChapter(chapters.get(chapter_position), manga.getName());
            if (ChapterFragment.getAdapter() != null)
                ChapterFragment.getAdapter().getItem(chapter_position).setIsFav(false);
            chapters.get(chapter_position).setIsFav(false);
            fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_unfav));
        } else {
            db.favChapter(chapters.get(chapter_position), manga.getName());
            if (ChapterFragment.getAdapter() != null)
                ChapterFragment.getAdapter().getItem(chapter_position).setIsFav(true);
            chapters.get(chapter_position).setIsFav(true);
            fv_button.setIcon(ContextCompat.getDrawable(ReadActivity.this, R.drawable.ic_action_star_fav));
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
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
        currentPage = viewPager.getCurrentItem();
        LoadChapterTask task = new LoadChapterTask(false, progressDialog);
        isRefreshing = true;
        task.execute(chapters.get(chapter_position).getUrl());
    }

    public class LoadChapterTask extends AsyncTask<String, Void, ArrayList<Page>> {

        private ProgressDialog dialog;
        private boolean isFromList;

        public LoadChapterTask(boolean fromList, ProgressDialog p) {
            dialog = p;
            isFromList = fromList;
        }

        public void onPreExecute() {
            next.setVisibility(ImageView.INVISIBLE);
            previous.setVisibility(ImageView.INVISIBLE);
            if (!isControllerShowing)
                showControllers();
            if (!isFromList) {
                if (dialog != null)
                    dialog.show();
            }
        }

        public ArrayList<Page> doInBackground(String... params) {
            MangaLibrary download = new MangaLibrary(params[0]);
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
            if (!isFromList) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }
            if (result != null) {
                FullScreenImageAdapter adapter = new FullScreenImageAdapter(ReadActivity.this, result);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(currentPage);
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
                viewPager.setOffscreenPageLimit(5);
                viewPager.setPageMargin(calculatedPixel);
                String text = currentPage == 0 ? "1/" + adapter.getCount() : (currentPage + 1) + "/" + adapter.getCount();
                pageIndicator.setText(text);
                progressBar.setProgress(0);
                increment = 0;
                progressBar.setMax(adapter.getCount());
                progressBar.setVisibility(View.VISIBLE);

                seekBar.setProgress(currentPage);
                seekBar.setMax(adapter.getCount() - 1);

                isRefreshing = false;

                getFavoriteStatus();
                setRead();
                setTitle(chapters.get(chapter_position).getName());

                if(isFromList)  mHideHandler.postDelayed(hideControllerThread, 5000);

            } else {
                if (!isFromList) {
                    Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_SHORT).show();
                    if (!isRefreshing)
                        chapter_position = chapter_position_temp;
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
                } else {
                    Toast.makeText(getApplicationContext(), network_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
