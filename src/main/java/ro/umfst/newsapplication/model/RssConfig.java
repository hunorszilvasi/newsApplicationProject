package ro.umfst.newsapplication.model;
/**
 *
 * @author hunorszilvasi
 */
import java.util.List;

// Rss articles
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