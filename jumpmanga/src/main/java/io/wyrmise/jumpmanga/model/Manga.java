package io.wyrmise.jumpmanga.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Manga implements Parcelable {
    private String name;
    private String url;
    private String description;
    private String image;
    private String latest;
    private Chapter chapter;
    private boolean isFav;

    public Manga(){

    }

    public Manga(String n, String i) {
        name = n;
        image = i;
        isFav = false;
    }

    public Manga(String n, String i, Chapter c) {
        name = n;
        image = i;
        chapter = c;
        isFav = false;
    }

    public Manga(String n, String u, String i, String l) {
        name = n;
        url = u;
        image = i;
        latest = l;
        isFav = false;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setIsFav(boolean isFav) {
        this.isFav = isFav;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLatest() {
        return latest;
    }

    public void setLatest(String latest) {
        this.latest = latest;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public String toString() {
        return name + ": " +latest;
    }

    protected Manga(Parcel in) {
        name = in.readString();
        url = in.readString();
        description = in.readString();
        image = in.readString();
        latest = in.readString();
        isFav = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeString(latest);
        dest.writeByte((byte) (isFav ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Manga> CREATOR = new Parcelable.Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel in) {
            return new Manga(in);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };
}