package ro.umfst.newsapplication.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *
 * @author hunorszilvasi
 */
public class HttpHelper {

    public static HttpURLConnection openConnection(String urlAddress)
            throws IOException {

        URL url = new URL(urlAddress);
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Follow redirects and handle the final response.
        connection.setInstanceFollowRedirects(true);

        return connection;
    }

    public static String describeStatus(int statusCode) {
        switch (statusCode) {
            case HttpURLConnection.HTTP_OK:
                return "OK";
            case HttpURLConnection.HTTP_MOVED_PERM:
                return "Moved Permanently";
            case HttpURLConnection.HTTP_MOVED_TEMP:
                return "Found (Temporary Redirect)";
            case HttpURLConnection.HTTP_BAD_REQUEST:
                return "Bad Request";
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return "Unauthorized";
            case HttpURLConnection.HTTP_FORBIDDEN:
                return "Forbidden";
            case HttpURLConnection.HTTP_NOT_FOUND:
                return "Not Found";
            case 429:
                return "Too Many Requests";
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return "Internal Server Error";
            case HttpURLConnection.HTTP_BAD_GATEWAY:
                return "Bad Gateway";
            case HttpURLConnection.HTTP_UNAVAILABLE:
                return "Service Unavailable";
            case 504:
                return "Gateway Timeout";
            default:
                if (statusCode >= 300 && statusCode < 400) {
                    return "Redirect";
                }
                if (statusCode >= 400 && statusCode < 500) {
                    return "Client Error";
                }
                if (statusCode >= 500 && statusCode < 600) {
                    return "Server Error";
                }
                return "Unknown Status";
        }
    }
}