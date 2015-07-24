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

import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.wyrmise.jumpmanga.activities.MainActivity;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;

public class FetchLatestService extends Service {

    private PowerManager.WakeLock wakeLock;
    private JumpDatabaseHelper db;
    private int index = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wakelock service");
        wakeLock.acquire();

        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        try {
            db = new JumpDatabaseHelper(this);

            ArrayList<Manga> mangas = db.getAllFavoritedMangas();
            if (mangas != null)
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
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

    private class FetchLatestTask extends AsyncTask<ArrayList<Manga>, Void, ArrayList<Manga>> {

        @Override
        protected ArrayList<Manga> doInBackground(ArrayList<Manga>... mangas) {
            try {
                ArrayList<Manga> list = mangas[0];
                ArrayList<Manga> tempNotificationArr = new ArrayList<>();
                DownloadUtils downloadUtils = new DownloadUtils();
                for (int i = 0; i < list.size(); i++) {
                    Manga manga = list.get(i);
                    Chapter latest = downloadUtils.GetLatestChapter(manga);
                    if (latest != null)
                        if (!latest.getName().equals(manga.getLatest()) && !latest.getName().equals("")) {
                            manga.setLatest(latest.getName());
                            manga.setChapter(latest);
                            if (db.isMangaFavorited(manga.getName())) {
                                db.updateLatestChapter(manga);
                                tempNotificationArr.add(manga);
                            }
                        }
                }
                return tempNotificationArr;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            writeLog();
            if (result != null && result.size() > 0) {
                for (int i = 0; i < result.size(); i++) {
                    db.insertSubscription(result.get(i), result.get(i).getChapter());
                }
                showNotification(result);
            }
        }
    }

    private void showNotification(ArrayList<Manga> mangas) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        if (mangas.size() == 1) {
            mBuilder.setContentTitle(mangas.get(0).getName());
            mBuilder.setContentText("New chapter: " + mangas.get(0).getLatest());
        } else {
            mBuilder.setContentTitle("New chapters");
            mBuilder.setContentText("New chapters found for your favorite manga");
        }

        mBuilder.setSmallIcon(R.drawable.ic_stat_notification);

        mBuilder.setNumber(mangas.size());

        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        for (int i = 0; i < mangas.size(); i++) {
            inboxStyle.addLine(mangas.get(i).toString());
        }

        mBuilder.setStyle(inboxStyle);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("favorite", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        note.defaults |= Notification.DEFAULT_LIGHTS;

        /* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(1, mBuilder.build());

    }

    private void writeLog() {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("jump_service_log.txt", MODE_APPEND));


            SimpleDateFormat format = new SimpleDateFormat(
                    "dd-M-yyyy hh:mm:ss", Locale.US);
            Date date = new Date();
            String dateFormat = "Service #" + index + ": at " + format.format(date);

            index++;

            out.write(dateFormat);
            out.write('\n');

            out.close();

        } catch (java.io.IOException e) {
        }
    }
}
