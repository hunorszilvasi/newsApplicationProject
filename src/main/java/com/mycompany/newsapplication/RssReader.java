package com.mycompany.newsapplication;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.net.URL;
import java.time.LocalDateTime;

public class RssReader {

    public void testFeed() {

        String rssUrl = "https://marosvasarhelyi.info/feed";

        try {

            URL url = new URL(rssUrl);

            SyndFeed feed = new SyndFeedInput()
                    .build(new XmlReader(url));

            System.out.println("Feed title: " + feed.getTitle());
            System.out.println("--------------------------------");
            DatabaseManager.initializeDatabase();
            ArticleDao dao = new ArticleDao();
            for (SyndEntry entry : feed.getEntries()) {
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
                dao.save(article);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}