package ro.umfst.newsapplication.service;

import org.jsoup.Jsoup;

public class HtmlUtils {

    public static String cleanHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return Jsoup.parse(html).text();
    }
}