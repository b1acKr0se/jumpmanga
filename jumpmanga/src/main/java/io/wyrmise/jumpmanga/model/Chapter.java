package io.wyrmise.jumpmanga.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Chapter implements Parcelable {

    private String mangaName;
    private String name;
    private String url;
    private boolean isRead;
    private boolean isFav;
    private ArrayList<String> path;

    public Chapter() {

    }

    public Chapter(String n){
        name = n;
        isRead = false;
        isFav = false;
    }

    public Chapter(String n, String u) {
        name = n;
        url = u;
        isRead = false;
        isFav = false;
    }

    public void setPath(ArrayList<String> p)  {
        path = p;
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setIsFav(boolean isFav) {
        this.isFav = isFav;
    }

    public String getMangaName() {
        return mangaName;
    }

    public void setMangaName(String mangaName) {
        this.mangaName = mangaName;
    }


    protected Chapter(Parcel in) {
        mangaName = in.readString();
        name = in.readString();
        url = in.readString();
        isRead = in.readByte() != 0x00;
        isFav = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mangaName);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeByte((byte) (isRead ? 0x01 : 0x00));
        dest.writeByte((byte) (isFav ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}