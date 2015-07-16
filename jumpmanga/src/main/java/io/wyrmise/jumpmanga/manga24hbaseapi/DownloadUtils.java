package io.wyrmise.jumpmanga.manga24hbaseapi;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.model.Page;

/**
 * Created by Thanh on 6/29/2015.
 */
public class DownloadUtils {

    private String url;
    private Document document;

    public DownloadUtils() {

    }

    public DownloadUtils(String u) {
        url = u;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Manga> GetMangas(int number_of_entries) {
        ArrayList<Manga> mangas = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("div.col-md-12.box").select("div");
            Element content = elements.get(2);
            Elements mangaList = content.select("div.panel-featured-row.panel-featured-row-small");

            for (Element e : mangaList.select("div.col-md-4.col-xs-12.col-sm-12").select("div.featured-item-small")) {
                String img = e.select("a.featured-item-small-a").select("img").attr("data-original");
                String name = e.select("h4").select("a[href]").text();
                String url = e.select("h4").select("a[href]").attr("abs:href");
                String latest = e.select("span.featured-item-new-chapt").select("a[href]").text();
                if(latest.equals(""))
                    latest = "Unknown";
                Manga manga = new Manga(name,url,img,latest);
                mangas.add(manga);
            }
            return mangas;
        } catch (IOException e) {

        }
        return null;
    }

    public ArrayList<Manga> GetMangasFromCategory() {
        ArrayList<Manga> mangas = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("div.col-md-12.box").select("div");
            Elements mangaList = elements.select("div.panel-featured-row.panel-featured-row-small");
            for (Element e : mangaList.select("div.col-md-4.col-xs-12.col-sm-12").select("div.featured-item-small")) {
                String img = e.select("a.featured-item-small-a").select("img").attr("data-original");
                String name = e.select("h4").select("a[href]").text();
                String url = e.select("h4").select("a[href]").attr("abs:href");
                String latest = e.select("span.featured-item-new-chapt").select("a[href]").text();
                if(latest.equals(""))
                    latest = "Unknown";
                Manga manga = new Manga(name,url,img,latest);
                mangas.add(manga);
            }
            return mangas;
        } catch (IOException e) {

        }
        return null;
    }

    public String GetMangaDetail() {
        try {
            String str = "";
            Document document = Jsoup.connect(url).get();
            Element table = document.select("div#manga-detail-tab-info.tab-pane.active.fade.in").select("table.table").first();
            str = table.text();
            System.out.println(str);
            StringBuffer stringBuffer = new StringBuffer(str);
            stringBuffer.insert(str.indexOf("Tác giả"), "\n");
            stringBuffer.insert(str.indexOf("Tình trạng") + 1, "\n");
            stringBuffer.insert(str.indexOf("Nguồn") + 2, "\n");
            stringBuffer.insert(str.indexOf("Lượt xem") + 3, "\n");
            stringBuffer.insert(str.indexOf("Ngày đăng") + 4, "\n");

            System.out.println(stringBuffer.toString().trim());
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Thể loại:\nTác giả:\nTình trạng:\nNguồn:\nLượt xem:\nNgày đăng:\n";
    }

    public String GetMangaSummary() {
        try {
            Element element = document.select("div#manga-detail-info.row.detail-info").select("div").select("small").first();
            String summary = element.text();
            System.out.println(summary);

            if (summary.startsWith(" "))
                return summary.substring(1, summary.length());

            return summary;
        }catch (NullPointerException e) {
            return null;
        }
    }

    public ArrayList<Chapter> GetChapters() {
        ArrayList<Chapter> chapters = new ArrayList<>();
        try {

            Document document = Jsoup.connect(url).get();

            Element table = document.select("table.table.chapt-table").first();

            Elements els = table.select("tr.item-odd,tr.item-even");

            for (int i = 0; i < els.size(); i++) {
                Chapter c = new Chapter();
                c.setName(els.get(i).select("a[href]").text());
                c.setUrl(els.get(i).select("a[href]").attr("abs:href"));
                chapters.add(c);
            }
            return chapters;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Page> GetPages() {
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
                pages.add(p);
            }

            return pages;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String GetLatestChapter(Manga manga) {
        String url = manga.getUrl();
        try {
            Document document = Jsoup.connect(url).get();
            Element e = document.select("div.col-md-6.wrap-chapt-chosen-info").first();
            String latest = e.select("a[href]").text();
            return latest;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ArrayList<Manga> GetAllMangas(String url) {
        ArrayList<Manga> mangas = new ArrayList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Element table = document.select("div.col-md-12.box").select("div").select("table.table").first();
            Elements els = table.select("tr.panel-update-each-item.panel-even,tr.panel-update-each-item.panel-odd");
            for (int i = 0; i < els.size(); i++) {
                Elements e = els.get(i).select("td.panel-update-each-item-title ");
                Manga manga = new Manga();
                String name = e.select("a[href]").attr("title");
                if(name.isEmpty())
                    name = e.select("a.tooltip-content-title[href]").text();
                System.out.println("Name : " +name);
                String manga_url = e.select("a[href]").attr("abs:href");
                System.out.println(manga_url);
                String img = e.select("img").attr("data-original");
                System.out.println(img);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
