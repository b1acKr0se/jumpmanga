package io.demiseq.jetreader.model;

/**
 * Created by Thanh on 6/29/2015.
 */
public class Page {
    private int page_num;
    private String url;

    public Page(){

    }

    public Page(int num, String u){
        page_num = num;
        url = u;
    }

    public int getPage_num() {
        return page_num;
    }

    public void setPage_num(int page_num) {
        this.page_num = page_num;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
