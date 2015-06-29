package io.wyrmise.jumpmanga;

import java.util.ArrayList;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Chapter {
    private String name;
    private String url;
    private int no_of_pages;
    private ArrayList<Page> pages;

    public Chapter(){

    }

    public Chapter(String n, String u, int no, ArrayList<Page> p) {
        name = n;
        url = u;
        no_of_pages = no;
        pages = p;
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

    public int getNo_of_pages() {
        return no_of_pages;
    }

    public void setNo_of_pages(int no_of_pages) {
        this.no_of_pages = no_of_pages;
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
    }
}
