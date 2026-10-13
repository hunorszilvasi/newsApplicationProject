package ro.umfst.newsapplication.model;

/**
 *
 * @author hunorszilvasi
 */
public class RssSource {
    // JSON article look
    private String name;
    private String url;
    private String language;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
