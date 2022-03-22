package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final List<NameValuePair> query;
    private final List<String> headers;
    private final String body;

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.query = URLEncodedUtils.parse(getOnlyQuery(path), Charset.defaultCharset());
        this.path = getOnlyPath(path);
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getQuery() {
        return query;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public List<String> getRequestKey() {
        return List.of(method, path);
    }

    public static String getOnlyPath(String path) {
        return (path.split("\\?"))[0];
    }

    public static String getOnlyQuery(String path) {
        int i = path.indexOf("?");
        if (i > -1 && i < path.length() - 1) {
            return path.substring(i + 1);
        }
        return null;
    }
}
