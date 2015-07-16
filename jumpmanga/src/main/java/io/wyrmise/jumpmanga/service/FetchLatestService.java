package io.wyrmise.jumpmanga.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.MainActivity;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Manga;

public class FetchLatestService extends Service {

    private PowerManager.WakeLock wakeLock;
    private JumpDatabaseHelper db;
    private int numberOfNotifications = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wakelock service");
        wakeLock.acquire();

        Toast.makeText(this,"Service started",Toast.LENGTH_SHORT).show();

        try {
            db = new JumpDatabaseHelper(this);
            ArrayList<Manga> mangas = db.getAllFavoritedMangas();
            new FetchLatestTask().execute(mangas);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        wakeLock.release();
        Toast.makeText(this, "Service stopped",Toast.LENGTH_SHORT).show();
    }

    private class FetchLatestTask extends AsyncTask<ArrayList<Manga>, Void, ArrayList<Manga>> {

        @Override
        protected ArrayList<Manga> doInBackground(ArrayList<Manga>... mangas) {
            ArrayList<Manga> list = mangas[0];
            ArrayList<Manga> tempNotificationArr = new ArrayList<>();
            DownloadUtils downloadUtils = new DownloadUtils();
            for(int i = 0; i < list.size(); i++) {
                Manga manga = list.get(i);
                String latest = downloadUtils.GetLatestChapter(manga);
                if (!latest.equals(manga.getLatest())) {
                    manga.setLatest(latest);
                    if (db.isMangaFavorited(manga.getName())) {
                        db.updateLatestChapter(manga);
                        tempNotificationArr.add(manga);
                    }
                }
            }
            return tempNotificationArr;
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            if(result.size()>0) {
                showNotification(result);
            }
        }
    }

    private void showNotification(ArrayList<Manga> mangas) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        if(mangas.size()==1) {
            mBuilder.setContentTitle(mangas.get(0).getName());
            mBuilder.setContentText("New chapter: " +mangas.get(0).getLatest());
        } else {
            mBuilder.setContentTitle("New chapters");
            mBuilder.setContentText("New chapters found for your favorite manga");
        }

        mBuilder.setSmallIcon(R.drawable.splash_icon);

        mBuilder.setNumber(mangas.size());

        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        for(int i = 0 ; i < mangas.size(); i++) {
            inboxStyle.addLine(mangas.get(i).toString());
        }

        mBuilder.setStyle(inboxStyle);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.putExtra("favorite", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_LIGHTS;

        /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(1, mBuilder.build());

    }
}
