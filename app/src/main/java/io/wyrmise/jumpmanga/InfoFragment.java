package io.wyrmise.jumpmanga;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment{

    public static final String ARG_IMAGE_URL = "image_url";
    private String[] str;
    private TextView detail, summary;
    private CardView descriptionCardView, plotCardView;
    private FloatingActionButton fab;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String image = ((DetailedActivity) getActivity()).getImage();

        String url = ((DetailedActivity) getActivity()).getUrl();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        setHasOptionsMenu(false);

        new GetMangaDetails().execute(url);

        ImageView img = (ImageView) view.findViewById(R.id.image);

        Picasso.with(getActivity().getApplicationContext()).load(image).into(img);


        detail = (TextView) view.findViewById(R.id.detail);


        summary = (TextView) view.findViewById(R.id.description);

        descriptionCardView = (CardView) view.findViewById(R.id.cardView);

        plotCardView = (CardView) view.findViewById(R.id.cardView2);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        return view;
    }


    public class GetMangaDetails extends AsyncTask<String, Void, String[]> {
        public String[] doInBackground(String... params) {
            str = new String[2];
            DownloadUtils download = new DownloadUtils(params[0]);
            str[0] = download.GetMangaDetail();
            str[1] = download.GetMangaSummary();


            return str;
        }

        public void onPostExecute(String[] str) {
            detail.setText(str[0]);
            summary.setText(str[1]);

            slideIn(descriptionCardView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    slideIn(plotCardView);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fadeIn(fab);
                        }
                    }, 500);
                }
            }, 300);

        }
    }

    public void fadeIn(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.fade_in);
            view.startAnimation(in);
            view.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void slideIn(View view) {
        try {
            Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
            view.startAnimation(in);
            view.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
