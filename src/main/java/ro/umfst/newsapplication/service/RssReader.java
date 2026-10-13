package ro.umfst.newsapplication.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import ro.umfst.newsapplication.dao.ArticleDao;
import java.sql.Timestamp;
import ro.umfst.newsapplication.dao.DatabaseManager;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.model.RssSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import ro.umfst.newsapplication.service.HttpHelper;
import java.net.URL;
import java.util.List;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

public class RssReader {

    private static final Logger LOGGER = Logger.getLogger(RssReader.class.getName());
    RSSSourceLoader loader = new RSSSourceLoader(); 
    List<RssSource> sources = loader.loadSources(); // Loading in the sources
     
    public void testFeed() {
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            return;
        }

        ArticleDao dao = new ArticleDao();

        int saved = 0;
        int failed = 0;
        int skipped = 0;

        for (RssSource source : sources) {
            HttpURLConnection connection = null;

            try {
                connection = HttpHelper.openConnection(source.getUrl());
                int statusCode = connection.getResponseCode();

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    LOGGER.warning(
                            "Could not process RSS source '" + source.getName()
                            + "': HTTP " + statusCode + " ("
                            + HttpHelper.describeStatus(statusCode) + ")"
                    );
                    failed++;
                    continue;
                }

                SyndFeed feed;
                try (XmlReader reader = new XmlReader(connection.getInputStream())) {
                    feed = new SyndFeedInput().build(reader);
                }

                for (SyndEntry entry : feed.getEntries()) {
                    Article article = toArticle(entry, source);
                    article.setSource(source.getName());

                    int result = dao.save(article);

                    if (result > 0) {
                        saved++;
                    } else if (result == 0) {
                        skipped++;
                    } else {
                        failed++;
                    }
                }

            } catch (SocketTimeoutException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Request timed out for RSS source: " + source.getName(),
                        e
                );
                failed++;

            } catch (IOException e) {
                LOGGER.log(
                        Level.WARNING,
                        "Connection failed for RSS source: " + source.getName(),
                        e
                );
                failed++;

            } catch (Exception e) {
                LOGGER.log(
                        Level.WARNING,
                        "Couldn't process feed: " + source.getName(),
                        e
                );
                failed++;

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        LOGGER.info(
                "Saved: " + saved
                + ", Failed: " + failed
                + ", Skipped: " + skipped
        );
    }
    
    // Creating article from the requested RSS FEED SyndEntry
    private Article toArticle(SyndEntry entry, RssSource source) {
        Article article = new Article();

        // Basic
        article.setTitle(entry.getTitle());
        article.setUrl(entry.getLink());
        article.setAuthor(entry.getAuthor());

        // Description
        if (entry.getDescription() != null) {
            article.setDescription(
                HtmlUtils.cleanHtml(entry.getDescription().getValue())
            );
        } else {
            article.setDescription("");
        }

        // Publisher
        article.setPublisher(source.getName());

        // Language
        article.setLanguage(source.getLanguage());

        // Default cover image
        article.setCoverImage(ImageExtractor.extractImage(entry));

        // Publication date
        if (entry.getPublishedDate() != null) {
            article.setPublishDate(
                new Timestamp(entry.getPublishedDate().getTime())
            );
        }

        // Database timestamps
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        article.setCreatedAt(now);
        article.setLastModifiedAt(now);

        return article;
    }
}