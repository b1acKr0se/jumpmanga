package io.wyrmise.jumpmanga.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.DownloadedReadActivity;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Page;
import io.wyrmise.jumpmanga.utils.FileDownloader;
import io.wyrmise.jumpmanga.utils.FileUtils;
import io.wyrmise.jumpmanga.utils.NotificationUtils;

/**
 * Created by Thanh on 7/19/2015.
 */
public class DownloaderService extends Service {

    private static ArrayList<Chapter> list;
    private FileUtils fileUtils;
    private String image;
    private int numberOfService = 0;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        fileUtils = new FileUtils();
        image = intent.getStringExtra("image");
        String mangaName = intent.getStringExtra("mangaName");
        String chapterName = intent.getStringExtra("chapterName");
        String url = intent.getStringExtra("chapterUrl");


        if (notificationManager == null && builder == null) {
            notificationManager = (NotificationManager) DownloaderService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(DownloaderService.this);
            builder.setContentTitle("Jump Manga")
                    .setContentText("Background downloads in progress...")
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setAutoCancel(false)
                    .setOngoing(true);
            Notification note = builder.build();

            notificationManager.notify(1337, builder.build());
            startForeground(1337, note);
        }
        numberOfService++;

        RetrieveAllPages task = new RetrieveAllPages(mangaName, chapterName);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);


    }

//    private static synchronized void getChapterToDownload(){
//        while(numberOfDownload<3 && list.size()>0) {
//            Chapter c = list.get(0);
//            new DownloadService().execute();
//            numberOfDownload++;
//            list.remove(0);
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        System.out.println("Background work completed");
    }

    public class RetrieveAllPages extends AsyncTask<String, Void, ArrayList<Page>> {
        private String mangaName;
        private String chapterName;

        public RetrieveAllPages(String m, String c) {
            mangaName = m;
            chapterName = c;
        }

        public void onPreExecute() {
            Toast.makeText(DownloaderService.this, "Downloading...", Toast.LENGTH_LONG).show();
        }

        public ArrayList<Page> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
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
            if (result != null) {
                DownloadAsync task = new DownloadAsync(mangaName, chapterName);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result);
            } else {
                Toast.makeText(DownloaderService.this, "Failed to download this chapter, please check your network", Toast.LENGTH_SHORT).show();
            }

        }
    }

    class DownloadAsync extends AsyncTask<ArrayList<Page>, Integer, Boolean> {
        private String mangaName;
        private String chapterName;
        private int id = NotificationUtils.getID();
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private PendingIntent pendingIntent;

        public DownloadAsync(String m, String c) {
            mangaName = m;
            chapterName = c;
        }

        @Override
        public void onPreExecute() {
            mNotifyManager = (NotificationManager) DownloaderService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(DownloaderService.this);
            mBuilder.setContentTitle(mangaName)
                    .setContentText("Download in progress: " + chapterName)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setAutoCancel(false)
                    .setOngoing(true);
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }

        public Boolean doInBackground(ArrayList<Page>... page) {

            ArrayList<Page> pages = page[0];

            try {

                FileDownloader downloader = new FileDownloader(mangaName, chapterName);

                downloader.downloadPoster(image);

                for (int i = 0; i < pages.size(); i++) {
                    downloader.download(pages.get(i).getUrl());
                    publishProgress((int) ((i * 100) / pages.size()));
                }
            } catch (Exception e) {
                return false;
            }

            Intent intent = new Intent(DownloaderService.this, DownloadedReadActivity.class);
            intent.putExtra("chapter_name", chapterName);
            if (fileUtils.isChapterDownloaded(mangaName, chapterName)) {
                intent.putStringArrayListExtra("image_path", fileUtils.getFilePaths());
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(DownloaderService.this, 0,
                    intent, 0);

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            mBuilder.setProgress(100, values[0], false);
            mNotifyManager.notify(id, mBuilder.build());
            super.onProgressUpdate(values);
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (result) {

                mBuilder.setContentText("Download completed: " + chapterName);
                mBuilder.setProgress(0, 0, false);
                mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                mBuilder.setAutoCancel(true);
                mBuilder.setOngoing(false);
//                mBuilder.setContentIntent(pendingIntent);
                mNotifyManager.notify(id, mBuilder.build());
            } else {
                System.out.println(fileUtils.deleteChapter(mangaName, chapterName));
                mBuilder.setContentText("Download failed: " + chapterName);
                mBuilder.setProgress(0, 0, false);
                mBuilder.setAutoCancel(true);
                mBuilder.setOngoing(false);
                mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                mNotifyManager.notify(id, mBuilder.build());
            }
            numberOfService--;
            if (numberOfService == 0) {
                System.out.println("Stopping service");
                stopSelf();
            }
        }
    }
}
