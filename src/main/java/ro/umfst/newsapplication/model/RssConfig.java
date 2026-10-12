package ro.umfst.newsapplication.model;

import java.util.List;

public class RssConfig {

    private List<RssSource> rssSources;

    public List<RssSource> getRssSources() {
        return rssSources;
    }

    public void setRssSources(List<RssSource> rssSources) {
        this.rssSources = rssSources;
    }
}