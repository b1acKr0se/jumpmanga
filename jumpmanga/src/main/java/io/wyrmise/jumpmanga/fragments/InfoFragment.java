package io.wyrmise.jumpmanga.fragments;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.DetailActivity;
import io.wyrmise.jumpmanga.activities.MainActivity;
import io.wyrmise.jumpmanga.animation.AnimationHelper;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    private JumpDatabaseHelper db;
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

        db = new JumpDatabaseHelper(context);

        setHasOptionsMenu(false);

        anim = new AnimationHelper(context);


        ImageView img = (ImageView) view.findViewById(R.id.image);

        if (!image.equals("") && image != null) {
            Picasso.with(context).load(image).into(img);
        } else {
            img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.error));
        }

        detail = (TextView) view.findViewById(R.id.detail);

        summary = (TextView) view.findViewById(R.id.description);

        descriptionCardView = (CardView) view.findViewById(R.id.cardView);

        plotCardView = (CardView) view.findViewById(R.id.cardView2);

        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        if (db.isMangaFavorited(manga.getName()))
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_favorite));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manga.isFav()) {
                    fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_unfavorite));
                    Snackbar.make(getView(), "Manga unfavorited", Snackbar.LENGTH_SHORT).show();
                    manga.setIsFav(false);
                    db.unfavoritedManga(manga.getName());
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_favorite));
                    Snackbar.make(getView(), "Manga favorited", Snackbar.LENGTH_SHORT).show();
                    db.insertManga(manga);
                    manga.setIsFav(true);

                    ArrayList<Manga> mangas = new ArrayList<Manga>();
                    for(int i = 0; i<5;i++)
                        mangas.add(manga);

                    showNotification(mangas);
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

    private void showNotification(ArrayList<Manga> mangas) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if(mangas.size()==1) {
            mBuilder.setContentTitle(mangas.get(0).getName());
            mBuilder.setContentText("New chapter: " +mangas.get(0).getLatest());
        } else {
            mBuilder.setContentTitle("New chapters");
            mBuilder.setContentText("New chapters found for your favorite manga");
        }

        mBuilder.setSmallIcon(R.drawable.ic_stat_notification);

        mBuilder.setNumber(mangas.size());

        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        for(int i = 0 ; i < mangas.size(); i++) {
            inboxStyle.addLine(mangas.get(i).toString());
        }

        mBuilder.setStyle(inboxStyle);

        Intent notificationIntent = new Intent(context,MainActivity.class);
        notificationIntent.putExtra("favorite", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_LIGHTS;

        /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(1, mBuilder.build());
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
