package io.wyrmise.jumpmanga.model;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Chapter;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Manga {
    private String name;
    private String url;
    private String description;
    private String image;
    private String latest;

    public Manga(String n, String i) {
        name = n;
        image = i;
    }

    public Manga(String n, String u, String i, String l) {
        name = n;
        url = u;
        image = i;
        latest = l;
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

    private ArrayList<Chapter> chapters;


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

    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }

    @Override
    public String toString() {
        return "Manga{" +
                "latest='" + latest + '\'' +
                ", image='" + image + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
