package ro.umfst.newsapplication.main;

import ro.umfst.newsapplication.ui.NewsFrame;
import ro.umfst.newsapplication.dao.ArticleDao;
import ro.umfst.newsapplication.model.*;
import ro.umfst.newsapplication.service.*;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import ro.umfst.newsapplication.dao.DatabaseManager;
import ro.umfst.newsapplication.ui.NewsFrame;

import java.util.List;
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
