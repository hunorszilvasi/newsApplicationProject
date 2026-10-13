package ro.umfst.newsapplication.service;

import com.rometools.rome.feed.synd.SyndEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ImageExtractor {

    public static String extractImage(SyndEntry entry) {

        // Extracting the image from the RSS feed
        if (entry.getDescription() != null) {
            String html = entry.getDescription().getValue();

            if (html != null && !html.isBlank()) {
                Document document = Jsoup.parse(html);
                Element image = document.selectFirst("img[src]");

                if (image != null) {
                    return image.absUrl("src").isBlank()
                            ? image.attr("src")
                            : image.absUrl("src");
                }
            }
        }

        // 2 No image was found
        return "images/asd.png";
    }
}