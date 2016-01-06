/*
 * Copyright (C) 2015 Hai Nguyen Thanh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.demiseq.jetreader.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;

import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.DownloadedReadActivity;
import io.demiseq.jetreader.activities.GeneralSettingsActivity;
import io.demiseq.jetreader.manga24hbaseapi.FetchingMachine;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.model.Page;
import io.demiseq.jetreader.utils.FileDownloader;
import io.demiseq.jetreader.utils.FileUtils;
import io.demiseq.jetreader.utils.NotificationUtils;

public class DownloadService extends Service {

    private static ArrayList<Chapter> list = new ArrayList<>();
    private FileUtils fileUtils;
    private String image;
    private static int numberOfDownload = 0;
    private int maxDownload = 3;
    private boolean isNotificationEnabled = true;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    SharedPreferences prefs;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String numberString = prefs.getString(GeneralSettingsActivity.KEY_DOWNLOAD_NUM, "3");
        maxDownload = Integer.parseInt(numberString);

        isNotificationEnabled = prefs.getBoolean(GeneralSettingsActivity.KEY_SHOW_NOTIFICATION, true);

        fileUtils = new FileUtils();
        image = intent.getStringExtra("image");

        ArrayList<Chapter> chapters = intent.getParcelableArrayListExtra("list");

        list.addAll(chapters);

        if (notificationManager == null && builder == null) {
            notificationManager = (NotificationManager) DownloadService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(DownloadService.this);
            builder.setContentTitle("Jump Manga")
                    .setContentText(getResources().getString(R.string.background_progress))
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setAutoCancel(false)
                    .setOngoing(true);
            Notification note = builder.build();

            notificationManager.notify(1337, builder.build());
            startForeground(1337, note);
        }

        if (list.size() > 0 && list != null) {
            getChapterToDownload();
        }
    }

    private void getChapterToDownload() {
        synchronized (list) {
            while (numberOfDownload < maxDownload && list.size() > 0) {
                Chapter c = list.get(0);
                RetrieveAllPages task = new RetrieveAllPages(c.getMangaName(), c.getName());
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, c.getUrl());
                numberOfDownload++;
                list.remove(0);
                builder.setContentText(getResources().getString(R.string.downloading) + " " + numberOfDownload + ". " + getResources().getString(R.string.queuing) + " " + list.size() + ".");
                notificationManager.notify(1337, builder.build());
            }
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
        stopForeground(true);
    }

    public class RetrieveAllPages extends AsyncTask<String, Void, ArrayList<Page>> {
        private String mangaName;
        private String chapterName;

        public RetrieveAllPages(String m, String c) {
            mangaName = m;
            chapterName = c;
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
            if (result != null) {
                DownloadAsync task = new DownloadAsync(mangaName, chapterName, isNotificationEnabled);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result);
            } else {
                numberOfDownload--;
                if (numberOfDownload == 0 && list.size() == 0) {
                    stopSelf();
                } else if (list.size() > 0) {
                    getChapterToDownload();
                }
            }

        }
    }

    class DownloadAsync extends AsyncTask<ArrayList<Page>, Integer, Boolean> {
        private String mangaName;
        private String chapterName;
        private boolean enabledNotification;
        private int id = NotificationUtils.getID();
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private PendingIntent pendingIntent;

        public DownloadAsync(String m, String c, boolean isEnabled) {
            mangaName = m;
            chapterName = c;
            enabledNotification = isEnabled;
        }

        @Override
        public void onPreExecute() {
            if (enabledNotification) {
                mNotifyManager = (NotificationManager) DownloadService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(DownloadService.this);
                mBuilder.setContentTitle(mangaName)
                        .setContentText(getResources().getString(R.string.downloading) + " " + chapterName)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setAutoCancel(false);
                mBuilder.setProgress(100, 0, false);
                mNotifyManager.notify(id, mBuilder.build());
            }
        }

        public Boolean doInBackground(ArrayList<Page>... page) {

            ArrayList<Page> pages = page[0];

            try {

                FileDownloader downloader = new FileDownloader(mangaName, chapterName);

                if (!fileUtils.hasPoster(mangaName))
                    downloader.downloadPoster(image);

                String fileExtension = pages.get(0).getUrl().substring(pages.get(0).getUrl().lastIndexOf("."));

                for (int i = 0; i < pages.size(); i++) {
                    String fileName;
                    if (i < 10)
                        fileName = "000" + i + fileExtension;
                    else if (i >= 10 && i < 100)
                        fileName = "00" + i + fileExtension;
                    else if (i >= 100 && i < 1000)
                        fileName = "0" + i + fileExtension;
                    else
                        fileName = i + fileExtension;
                    downloader.downloadAndRename(pages.get(i).getUrl(), fileName);
                    publishProgress((int) ((i * 100) / pages.size()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            if (enabledNotification) {
                Intent intent = new Intent(DownloadService.this, DownloadedReadActivity.class);
                intent.putExtra("manga_name", mangaName);
                intent.putExtra("chapter_name", chapterName);
                if (fileUtils.isChapterDownloaded(mangaName, chapterName)) {
                    intent.putStringArrayListExtra("image_path", fileUtils.getFilePaths());
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(DownloadService.this, id,
                        intent, 0);
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            if (enabledNotification) {
                mBuilder.setProgress(100, values[0], false);
                mNotifyManager.notify(id, mBuilder.build());
                super.onProgressUpdate(values);
            }
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (enabledNotification) {
                if (result) {
                    mBuilder.setContentText(getResources().getString(R.string.download_completed) + " " + chapterName);
                    mBuilder.setProgress(0, 0, false);
                    mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    mBuilder.setAutoCancel(true);
                    mBuilder.setContentIntent(pendingIntent);
                    mNotifyManager.notify(id, mBuilder.build());
                } else {
                    System.out.println(fileUtils.deleteChapter(mangaName, chapterName));
                    mBuilder.setContentText(getResources().getString(R.string.download_failed) + " " + chapterName);
                    mBuilder.setProgress(0, 0, false);
                    mBuilder.setAutoCancel(true);
                    mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }
            numberOfDownload--;
            if (numberOfDownload == 0 && list.size() == 0) {
                notificationManager = (NotificationManager) DownloadService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                builder = new NotificationCompat.Builder(DownloadService.this);
                builder.setContentTitle("Jump Manga")
                        .setContentText(getResources().getString(R.string.background_completed))
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setAutoCancel(true);
                notificationManager.notify(2304, builder.build());
                stopSelf();
            } else if (list.size() > 0) {
                getChapterToDownload();
            }
        }
    }
}
