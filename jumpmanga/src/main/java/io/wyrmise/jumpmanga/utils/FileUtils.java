package io.wyrmise.jumpmanga.utils;


import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class FileUtils {
    private String mangaName;
    private String chapterName;

    public boolean isChapterDownloaded(String m, String c) {
        File sdcard = Environment.getExternalStorageDirectory();
        File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/"+m+"/"+c);
        if(directory.exists() && directory.isDirectory() && directory.listFiles().length>0) {
            mangaName = m;
            chapterName = c;
            return true;
        }
        return false;
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();

        File sdcard = Environment.getExternalStorageDirectory();
        File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/"+mangaName+"/"+chapterName);

        // check for directory
        if (directory.isDirectory()) {
            // getting list of file paths
            File[] listFiles = directory.listFiles();

            // Check for count
            if (listFiles.length > 0) {

                // loop through all files
                for (int i = 0; i < listFiles.length; i++) {

                    // get file path
                    String filePath = listFiles[i].getAbsolutePath();

                        // Add image path to array list
                        filePaths.add(filePath);
                }
            }
        }
        return filePaths;
    }

    public boolean deleteChapter(String mangaName, String chapterName) {
        File sdcard = Environment.getExternalStorageDirectory();
        File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/"+mangaName+"/"+chapterName);
        if(directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                files[i].delete();
            }
        }
        return directory.delete();
    }
}
