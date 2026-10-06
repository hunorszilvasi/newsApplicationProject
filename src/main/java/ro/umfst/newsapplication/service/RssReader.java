package ro.umfst.newsapplication.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import ro.umfst.newsapplication.dao.ArticleDao;
import ro.umfst.newsapplication.dao.DatabaseManager;
import ro.umfst.newsapplication.model.Article;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RssReader {

    private static final Logger LOGGER = Logger.getLogger(RssReader.class.getName());
    private static final String RSS_URL = "https://marosvasarhelyi.info/feed";

    public void testFeed() {
        SyndFeed feed;

        // feed letoltese es exceptions
        try {
            URL url = new URL(RSS_URL);
            feed = new SyndFeedInput().build(new XmlReader(url));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Wrong URL" + RSS_URL, e);
            return;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Internet error", e);
            return;
        } catch (FeedException e) {
            LOGGER.log(Level.SEVERE, "Invalid feed", e);
            return;
        }

//        System.out.println("Feed title: " + feed.getTitle());
//        System.out.println("--------------------------------");

        // adatbázis inicializálása
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            return;
        }

        // 3. cikkek mentese + exception
        ArticleDao dao = new ArticleDao();
        int saved = 0;
        int failed = 0;
        int skipped = 0;

        for (SyndEntry entry : feed.getEntries()) {
            try {

                int result = dao.save(toArticle(entry));

                if (result > 0) {
                    saved++;
                } else if (result == 0) {
                    skipped++;
                } else {
                    failed++;
                }

            } catch (Exception e) {
                failed++;

                LOGGER.log(Level.WARNING,
                        "Couldn't save this article: " + entry.getLink(), e);
            }
        }
        

        LOGGER.info("Saved: " + saved + ", Failed: " + failed + ", Skipped: " + skipped);
    }

    private Article toArticle(SyndEntry entry) {
        Article article = new Article();
        article.setSource("Marosvasarhelyi Info");
        article.setTitle(entry.getTitle());
        if (entry.getPublishedDate() != null) {
            article.setPublishDate(entry.getPublishedDate().toString());
        }
        article.setUrl(entry.getLink());
        article.setAuthor(entry.getAuthor());
        article.setCreatedAt(LocalDateTime.now().toString());
        article.setLastModifiedAt(LocalDateTime.now().toString());
        return article;
    }
}