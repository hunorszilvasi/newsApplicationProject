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
import java.net.URL;
import java.util.List;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RssReader {

    private static final Logger LOGGER = Logger.getLogger(RssReader.class.getName());
    RSSSourceLoader loader = new RSSSourceLoader(); 
    List<RssSource> sources = loader.loadSources(); // betoltsuk a sourceokat
     
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

            try {

                URL url = new URL(source.getUrl());

                SyndFeed feed =
                        new SyndFeedInput().build(new XmlReader(url));

                for (SyndEntry entry : feed.getEntries()) {

                    Article article = toArticle(entry);

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

            } catch (Exception e) {

                LOGGER.log(Level.WARNING,
                        "Couldn't process feed: "
                        + source.getName(), e);

                failed++;
            }
        }
        LOGGER.info(
        "Saved: " + saved
        + ", Failed: " + failed
        + ", Skipped: " + skipped);

    }
    private Article toArticle(SyndEntry entry) {
        Article article = new Article();
        article.setTitle(entry.getTitle());
        if (entry.getPublishedDate() != null) {
            article.setPublishDate(
                new Timestamp(entry.getPublishedDate().getTime())
            );
        }
        article.setUrl(entry.getLink());
        article.setAuthor(entry.getAuthor());
        article.setCreatedAt(
        Timestamp.valueOf(LocalDateTime.now()));
        article.setLastModifiedAt(
        Timestamp.valueOf(LocalDateTime.now()));
        return article;
    }
}