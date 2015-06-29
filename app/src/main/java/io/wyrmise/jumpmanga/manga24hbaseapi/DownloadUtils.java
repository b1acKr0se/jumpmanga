package io.wyrmise.jumpmanga.manga24hbaseapi;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.wyrmise.jumpmanga.Chapter;
import io.wyrmise.jumpmanga.Page;

/**
 * Created by Thanh on 6/29/2015.
 */
public class DownloadUtils {

    public DownloadUtils() {

    }

    public int GetTotalNumberOfChapters(String url) {
        try {
            Document document = Jsoup.connect(url).get();

            Element table = document.select("table.table.chapt-table").first();

            Elements els = table.select("tr.item-odd,tr.item-even");

            System.out.println("Total num of chaps: " + els.size());

            return els.size();

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArrayList<Chapter> GetChapters(String url) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        try {

            Document document = Jsoup.connect(url).get();

            Element table = document.select("table.table.chapt-table").first();

            Elements els = table.select("tr.item-odd,tr.item-even");

            for (int i = 0; i < els.size(); i++) {
                Chapter c = new Chapter();
                c.setName(els.get(i).select("a[href]").text());
                System.out.println(c.getName());
                c.setUrl(els.get(i).select("a[href]").attr("abs:href"));
                System.out.println(c.getUrl());
                chapters.add(c);
            }
            return chapters;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Page> GetPages(String url) {
        ArrayList<Page> pages = new ArrayList<>();
        try {

            Document document = Jsoup.connect(url).get();

            Element content = document.select("div#manga-chapt-detail-tab-read").select("script").first();

            String html = content.html();

            String data = html.substring(html.indexOf("data='") + 6, html.indexOf("';"));

            String[] images = data.split("\\|");

            for (int i = 0; i < images.length; i++) {
                Page p = new Page();
                p.setPage_num(i + 1);
                p.setUrl(images[i]);
                System.out.println(images[i]);
                pages.add(p);
            }

            return pages;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
