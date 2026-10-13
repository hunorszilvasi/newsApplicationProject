package ro.umfst.newsapplication.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import ro.umfst.newsapplication.dao.ArticleDao;
import java.sql.Timestamp;
import ro.umfst.newsapplication.dao.DatabaseManager;
import ro.umfst.newsapplication.model.Article;
import ro.umfst.newsapplication.model.RssSource;
import java.io.IOException;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.util.List;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;
/**
 *
 * @author hunorszilvasi
 */

public class RssReader {

    private static final Logger LOGGER = Logger.getLogger(RssReader.class.getName());
    RSSSourceLoader loader = new RSSSourceLoader(); 
    List<RssSource> sources = loader.loadSources(); // Loading in the sources
     
    public RefreshResult refreshAll() {
        DatabaseManager.initializeDatabase();
        ArticleDao dao = new ArticleDao();

        int saved = 0, skipped = 0, failed = 0;
        List<String> failedSources = new ArrayList<>();

        for (RssSource source : sources) {
            HttpURLConnection connection = null;
            try {
                connection = HttpHelper.openConnection(source.getUrl());
                int statusCode = connection.getResponseCode();

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    failedSources.add(source.getName() + " (HTTP " + statusCode + " "
                            + HttpHelper.describeStatus(statusCode) + ")");
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
                    if (result > 0) saved++;
                    else if (result == 0) skipped++;
                    else failed++;
                }

            } catch (SocketTimeoutException e) {
                LOGGER.log(Level.WARNING, "Timeout: " + source.getName(), e);
                failedSources.add(source.getName() + " (timeout)");
                failed++;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Connection failed: " + source.getName(), e);
                failedSources.add(source.getName() + " (connection error)");
                failed++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Invalid feed: " + source.getName(), e);
                failedSources.add(source.getName() + " (invalid feed)");
                failed++;
            } finally {
                if (connection != null) connection.disconnect();
            }
        }

        LOGGER.info("Saved: " + saved + ", Skipped: " + skipped + ", Failed: " + failed);
        return new RefreshResult(saved, skipped, failed, failedSources);
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