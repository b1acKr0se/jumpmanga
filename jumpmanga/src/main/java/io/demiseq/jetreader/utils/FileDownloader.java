package io.demiseq.jetreader.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileDownloader {
    private String mangaName;
    private String chapterName;
    private boolean isSdCardAvailable;

    public FileDownloader(String m, String c) {
        mangaName = m;
        chapterName = c;
        if (canWriteOnExternalStorage()) isSdCardAvailable = true;
    }

    public void download(String fileUrl) throws IOException {
        if (isSdCardAvailable) {
            File sdcard = Environment.getExternalStorageDirectory();
            File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/" + mangaName + "/" + chapterName);
            directory.mkdirs();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            URL url = new URL(fileUrl);
            InputStream input = url.openStream();
            try {
                OutputStream output = new FileOutputStream(new File(directory, fileName));
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        }
    }

    public void downloadAndRename(String fileUrl, String fileName) throws IOException {
        if (isSdCardAvailable) {
            File sdcard = Environment.getExternalStorageDirectory();
            File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/" + mangaName + "/" + chapterName);
            directory.mkdirs();
            URL url = new URL(fileUrl);
            InputStream input = url.openStream();
            try {
                OutputStream output = new FileOutputStream(new File(directory, fileName));
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        }
    }

    public void downloadPoster(String fileUrl) {
        try {
            if (isSdCardAvailable) {
                File sdcard = Environment.getExternalStorageDirectory();
                File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/" + mangaName + "/");
                directory.mkdirs();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                URL url = new URL(fileUrl);
                InputStream input = url.openStream();
                try {
                    OutputStream output = new FileOutputStream(new File(directory, fileName));
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                            output.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    input.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            System.out.println("writing to sd card");
            return true;
        }
        return false;
    }
}
