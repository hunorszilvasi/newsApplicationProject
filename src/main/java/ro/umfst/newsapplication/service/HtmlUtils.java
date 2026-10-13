package ro.umfst.newsapplication.service;

import org.jsoup.Jsoup;

public class HtmlUtils {
    // Formating the Description so it wont be a HTML code in the GUI
    public static String cleanHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return Jsoup.parse(html).text();
    }
}