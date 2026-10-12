package ro.umfst.newsapplication.model;

import java.util.List;

// Rss cikkek 
public class RssConfig {

    private List<RssSource> rssSources;

    // getter setter
    public List<RssSource> getRssSources() {
        return rssSources;
    }

    public void setRssSources(List<RssSource> rssSources) {
        this.rssSources = rssSources;
    }
}