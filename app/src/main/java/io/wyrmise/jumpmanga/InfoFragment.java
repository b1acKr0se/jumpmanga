package io.wyrmise.jumpmanga;


import android.content.Context;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import io.wyrmise.jumpmanga.animation.AnimationHelper;
import io.wyrmise.jumpmanga.database.DatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    private DatabaseHelper db;
    private Context context;
    private String[] str;
    private TextView detail, summary;
    private CardView descriptionCardView, plotCardView;
    private FloatingActionButton fab;
    private AnimationHelper anim;
    private Manga manga;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String image = ((DetailActivity) getActivity()).getManga().getImage();

        final String url = ((DetailActivity) getActivity()).getManga().getUrl();

        manga = ((DetailActivity) getActivity()).getManga();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        context = getActivity().getApplicationContext();

        db = new DatabaseHelper(context);

        setHasOptionsMenu(false);

        anim = new AnimationHelper(context);


        ImageView img = (ImageView) view.findViewById(R.id.image);

        if (!image.equals("") && image != null) {
            Picasso.with(context).load(image).into(img);
        } else {
            img.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.error));
        }

        detail = (TextView) view.findViewById(R.id.detail);

        summary = (TextView) view.findViewById(R.id.description);

        descriptionCardView = (CardView) view.findViewById(R.id.cardView);

        plotCardView = (CardView) view.findViewById(R.id.cardView2);



        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        if (manga.isFav())
            fab.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_action_favorite));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manga.isFav()) {
                    fab.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_action_unfavorite));
                    Snackbar.make(getView(), "Manga unfavorited", Snackbar.LENGTH_SHORT).show();
                    manga.setIsFav(false);
                    db.unfavoritedManga(manga.getName());
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_favorite));
                    Snackbar.make(getView(), "Manga favorited", Snackbar.LENGTH_SHORT).show();
                    db.insertManga(manga);
                    manga.setIsFav(true);
                }

            }
        });

//        if (savedInstanceState != null) {
//            str = savedInstanceState.getStringArray("info");
//            detail.setText(str[0]);
//            summary.setText(str[1]);
//            descriptionCardView.setVisibility(CardView.VISIBLE);
//            plotCardView.setVisibility(CardView.VISIBLE);
//            fab.setVisibility(FloatingActionButton.VISIBLE);
//        } else
            new GetMangaDetails().execute(url);


        return view;
    }

//    @Override
//    public void onSaveInstanceState(Bundle bundle) {
//        if (str != null)
//            bundle.putStringArray("info", str);
//        super.onSaveInstanceState(bundle);
//
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    public class GetMangaDetails extends AsyncTask<String, Void, String[]> {
        public String[] doInBackground(String... params) {
            str = new String[2];
            DownloadUtils download = new DownloadUtils(params[0]);
            String detail = download.GetMangaDetail();
            String plot = download.GetMangaSummary();
            if (detail != null && plot != null) {
                str[0] = detail;
                str[1] = plot;
                return str;
            }
            return null;
        }

        public void onPostExecute(String[] str) {
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
                Toast.makeText(context, "Failed to retrieve manga info!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
