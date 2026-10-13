package ro.umfst.newsapplication.model;

import java.util.Comparator;
/**
 *
 * @author hunorszilvasi
 */

public class ArticleTitleComparator implements Comparator<Article> {

    @Override
    public int compare(Article a1, Article a2) {
        return a1.getTitle().compareToIgnoreCase(a2.getTitle()); //abc order
    }
}