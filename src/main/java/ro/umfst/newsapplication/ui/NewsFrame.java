package ro.umfst.newsapplication.ui;
import ro.umfst.newsapplication.model.Article;

import ro.umfst.newsapplication.dao.ArticleDao;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.util.List;

public class NewsFrame extends JFrame {

    public NewsFrame() {

        setTitle("News Reader");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ArticleDao dao = new ArticleDao();
        List<Article> articles = dao.findAll();

        String[] columns = {"ID", "Title", "Source"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        add(new JScrollPane(table));

        setVisible(true);
    }
}