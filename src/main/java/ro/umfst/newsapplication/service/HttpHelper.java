package ro.umfst.newsapplication.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

public class HttpHelper {
    // http status kod ellenorzes
    public static int getStatusCode(String urlAddress) {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlAddress);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            return connection.getResponseCode();

        } catch (IOException e) {
            System.err.println(
                    "HTTP request failed: " + e.getMessage()
            );

            return -1;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}