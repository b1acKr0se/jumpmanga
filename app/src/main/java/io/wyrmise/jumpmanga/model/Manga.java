package io.wyrmise.jumpmanga.model;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Chapter;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Manga {
    private String name;
    private String url;
    private int no_of_chapters;
    private String author;
    private String description;
    private String image;
    private String latest;

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

    public Manga(String n, String i){
        name = n;
        image = i;
    }

    public Manga(String n, String u, int no, String a, String d, ArrayList<Chapter> c) {
        name = n;
        url = u;
        no_of_chapters = no;
        author = a;
        description = d;
        chapters = c;
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

    public int getNo_of_chapters() {
        return no_of_chapters;
    }

    public void setNo_of_chapters(int no_of_chapters) {
        this.no_of_chapters = no_of_chapters;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
}
