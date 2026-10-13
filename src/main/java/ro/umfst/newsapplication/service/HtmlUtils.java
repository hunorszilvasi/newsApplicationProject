package ro.umfst.newsapplication.service;
/**
 *
 * @author hunorszilvasi
 */
import org.jsoup.Jsoup;

public class HtmlUtils {
    // Formating the Description
    public static String cleanHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return Jsoup.parse(html).text();
    }
}