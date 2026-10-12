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
import java.util.logging.Logger;

public class RssReader {

    private static final Logger LOGGER = Logger.getLogger(RssReader.class.getName());
    RSSSourceLoader loader = new RSSSourceLoader(); 
    List<RssSource> sources = loader.loadSources(); // betoltsuk a sourceokat
     
    public void testFeed() { // letolteni az adatokat rss feedbol

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
        //
        for (RssSource source : sources) {

            try {

                // http statusz kod mielott lekerjuk az rss feedet
                int statusCode = HttpHelper.getStatusCode(source.getUrl());

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    LOGGER.warning(
                            "RSS source returned HTTP status "
                            + statusCode + ": " + source.getName()
                    );

                    failed++;
                    continue;
                }

                URL url = new URL(source.getUrl());

                SyndFeed feed =
                        new SyndFeedInput().build(new XmlReader(url));

                for (SyndEntry entry : feed.getEntries()) {

                    Article article = toArticle(entry, source);
                    article.setSource(source.getName());
                    
                    // adatbazisba mentes
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

                LOGGER.log(
                        Level.WARNING,
                        "Couldn't process feed: " + source.getName(),
                        e
                );

                failed++;
            }
        }
        //
        LOGGER.info(
        "Saved: " + saved
        + ", Failed: " + failed
        + ", Skipped: " + skipped);

    }
    
    // kezelheto article formatumba SyndEntry-bol
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