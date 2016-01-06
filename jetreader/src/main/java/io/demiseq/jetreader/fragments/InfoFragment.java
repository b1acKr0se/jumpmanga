package io.demiseq.jetreader.fragments;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.DetailActivity;
import io.demiseq.jetreader.animation.AnimationHelper;
import io.demiseq.jetreader.database.JumpDatabaseHelper;
import io.demiseq.jetreader.api.MangaLibrary;
import io.demiseq.jetreader.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    private JumpDatabaseHelper db;
    private Context context;
    private String[] str;
    private AnimationHelper anim;
    private Manga manga;

    @Bind(R.id.cardView) CardView descriptionCardView;
    @Bind(R.id.cardView2) CardView plotCardView;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.detail) TextView detail;
    @Bind(R.id.description) TextView summary;
    @Bind(R.id.image) ImageView img;
    @Bind(R.id.reload) Button reload;
    @Bind(R.id.progress_bar)ProgressBar progressBar;

    @BindString(R.string.manga_fav) String manga_fav;
    @BindString(R.string.manga_unfav) String manga_unfav;
    @BindString(R.string.info_retrieve_failed) String retrieve_failed;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        manga = ((DetailActivity) getActivity()).getManga();

        final String image = manga.getImage();

        final String url = manga.getUrl();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        context = getActivity().getApplicationContext();

        ButterKnife.bind(this, view);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        db = new JumpDatabaseHelper(context);

        setHasOptionsMenu(false);

        anim = new AnimationHelper(context);

        if (!image.equals("") && image != null) {
            Glide.with(context).load(image).into(img);
        } else {
            img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.error));
        }

        if (db.isMangaFavorited(manga.getName()))
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_favorite));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manga.isFav()) {
                    fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_unfavorite));
                    Snackbar.make(getView(), manga_unfav, Snackbar.LENGTH_SHORT).show();
                    manga.setIsFav(false);
                    db.unfavoritedManga(manga.getName());
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_favorite));
                    Snackbar.make(getView(), manga_fav, Snackbar.LENGTH_SHORT).show();
                    db.insertManga(manga);
                    manga.setIsFav(true);
                }
            }
        });

        new GetMangaDetails().execute(url);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    public class GetMangaDetails extends AsyncTask<String, Void, String[]> {


        @Override
        public void onPreExecute() {
            reload.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public String[] doInBackground(String... params) {
            try {
                str = new String[2];
                MangaLibrary download = new MangaLibrary(params[0]);
                String detail = download.GetMangaDetail();
                manga.setLatest(download.GetLatestChapter(manga).getName());
                if (db.isMangaFavorited(manga.getName())) {
                    db.updateLatestChapter(manga);
                }
                String plot = download.GetMangaSummary();
                if (detail != null && plot != null) {
                    str[0] = detail;
                    str[1] = plot;
                    return str;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(String[] str) {
            progressBar.setVisibility(View.GONE);

            if (str != null) {
                detail.setText(str[0]);
                summary.setText(str[1]);

                anim.slideIn(descriptionCardView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anim.slideIn(plotCardView);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                anim.fadeIn(fab);
                            }
                        }, 500);
                    }
                }, 300);
            } else {
                Toast.makeText(context, retrieve_failed, Toast.LENGTH_SHORT).show();
                reload.setVisibility(View.VISIBLE);
                reload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new GetMangaDetails().execute(manga.getUrl());
                    }
                });
            }
        }
    }

}
