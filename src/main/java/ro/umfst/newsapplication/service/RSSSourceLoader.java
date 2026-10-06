package ro.umfst.newsapplication.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ro.umfst.newsapplication.model.RssSource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

public class RSSSourceLoader {

    public List<RssSource> loadSources() {
        // beolvassa az rss.json filet (a sourceokat)
        Reader reader = new InputStreamReader(
                getClass().getClassLoader()
                        .getResourceAsStream("rss.json"));

        Type type = new TypeToken<List<RssSource>>() {}.getType();

        return new Gson().fromJson(reader, type);
    }
}