package io.wyrmise.jumpmanga.service;

import android.app.IntentService;
import android.content.Intent;

import io.wyrmise.jumpmanga.utils.FileUtils;

/**
 * Created by Thanh on 7/19/2015.
 */
public class DownloadService extends IntentService {

    private FileUtils fileUtils;

    private String image;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        fileUtils = new FileUtils();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        image = intent.getStringExtra("image");
        String mangaName = intent.getStringExtra("mangaName");
        String chapterName = intent.getStringExtra("chapterName");
        String url = intent.getStringExtra("chapterUrl");


    }
}
