package ro.umfst.newsapplication.service;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import ro.umfst.newsapplication.model.RssConfig;
import ro.umfst.newsapplication.model.RssSource;

public class RSSSourceLoader {
    // rss betoltese a json filebol
    public List<RssSource> loadSources() {

        Gson gson = new Gson();

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("rss.json")) {

            if (input == null) {
                throw new IllegalStateException(
                    "rss.json not found in resources."
                );
            }

            try (Reader reader = new InputStreamReader(
                    input, StandardCharsets.UTF_8)) {

                RssConfig config = gson.fromJson(reader, RssConfig.class);

                if (config == null || config.getRssSources() == null) {
                    throw new IllegalStateException(
                        "Invalid rss.json: rssSources is missing."
                    );
                }

                return config.getRssSources();
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load RSS sources from rss.json.", e
            );
        }
    }
}