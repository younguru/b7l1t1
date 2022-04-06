package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, List<String>> query;
    private final List<String> headers;
    private final String body;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final List<String> allowedMethods = List.of(GET, POST);
    private static final int bufferLimit = 4096;

    private Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.query = new HashMap<>();
        for (NameValuePair nameValuePair : URLEncodedUtils.parse(getOnlyQuery(path), Charset.defaultCharset())) {
            var paramList = query.get(nameValuePair.getName());
            if (paramList == null) {
                paramList = new ArrayList<>();
            }
            paramList.add(nameValuePair.getValue());
            this.query.put(nameValuePair.getName(), paramList);
        }
        this.path = getOnlyPath(path);
        this.headers = headers;
        this.body = body;
    }

    public static Request getInstance(BufferedInputStream in) throws IOException {
        in.mark(bufferLimit);
        final var buffer = new byte[bufferLimit];
        final var read = in.read(buffer);
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

        if (requestLineEnd == -1) {
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }
        System.out.println(method);

        var path = requestLine[1];
        if (!path.startsWith("/")) {
            return null;
        }
        // root redirect to index.html
        if ("/".equals(path)) {
            path = "/index.html";
        }
        System.out.println(path);

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println(headers);

        // для GET тела нет
        String body = null;
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                System.out.println(body);
            }
        }
        return new Request(method, path, headers, body);
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getQueryParam(String name) {
        return query.get(name);
    }

    public Map<String, List<String>> getQueryParams() {
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
