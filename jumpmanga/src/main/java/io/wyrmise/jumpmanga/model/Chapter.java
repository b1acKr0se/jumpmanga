package io.wyrmise.jumpmanga.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Chapter implements Parcelable {
    private String name;
    private String url;
    private boolean isRead;

    public Chapter() {

    }

    public Chapter(String n){
        name = n;
        isRead = false;
    }

    public Chapter(String n, String u) {
        name = n;
        url = u;
        isRead = false;
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


    protected Chapter(Parcel in) {
        name = in.readString();
        url = in.readString();
        isRead = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeByte((byte) (isRead ? 0x01 : 0x00));
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