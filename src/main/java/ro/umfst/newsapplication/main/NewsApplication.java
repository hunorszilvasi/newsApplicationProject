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

import java.util.List;
/**
 *
 * @author hunorszilvasi
 */
public class NewsApplication {

    public static void main(String[] args) {
        RssReader reader = new RssReader();
        reader.testFeed();
        new NewsFrame();
  
        
    }
}
