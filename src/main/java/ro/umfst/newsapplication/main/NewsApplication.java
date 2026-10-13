package ro.umfst.newsapplication.main;

import javax.swing.SwingUtilities;
import ro.umfst.newsapplication.dao.DatabaseManager;
import ro.umfst.newsapplication.ui.NewsFrame;

/**
 *
 * @author hunorszilvasi // MAIN
 */
public class NewsApplication {

    public static void main(String[] args) {
        // Create the tables first, then show the saved news without waiting for the network
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(NewsFrame::new); // creates it on a swing thread
    }
}
