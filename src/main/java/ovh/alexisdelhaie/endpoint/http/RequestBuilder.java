package ovh.alexisdelhaie.endpoint.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {

    public static int HTTP_PORT = 80;
    public static int HTTPS_PORT = 443;
    public static String HTTP_SCHEME = "http";
    public static String HTTPS_SCHEME = "https";

    private String host;
    private String scheme;
    private String path;
    private int port;
    private final HashMap<String, String> params;
    private final HashMap<String, String> customHeaders;
    private String body;

    public RequestBuilder (String url) throws MalformedURLException {
        params = new HashMap<>();
        customHeaders = new HashMap<>();
        scheme = (url.toLowerCase().startsWith("https://")) ? HTTPS_SCHEME : HTTP_SCHEME;
        url = (!url.toLowerCase().startsWith("https://")
                && !url.toLowerCase().startsWith("http://")) ? "http://" + url : url;
        URL u = new URL(url);
        host = u.getHost();
        path = (u.getPath().isBlank()) ? "/" : u.getPath();
        port = u.getPort();
        if (port == -1) {
            port = (scheme.equals(HTTPS_SCHEME)) ? HTTPS_PORT : HTTP_PORT;
        }
        parseParams(url);
    }

    private void parseParams(String url) {
        String[] p = url.split("[?]");
        if (p.length == 2) {
            String[] couples = p[1].split("[&]");
            for (String c: couples) {
                String[] v = c.split("[=]");
                if (v.length == 2) {
                    params.put(v[0], v[1]);
                }
            }
        }
    }

    public RequestBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public RequestBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public RequestBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public RequestBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public RequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public RequestBuilder setCustomHeaders(HashMap<String, String> custom) {
        this.customHeaders.clear();
        for (Map.Entry<String, String> entry : custom.entrySet()) {
            this.customHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return this;
    }

    public RequestBuilder addCustomHeader(String key, String value) {
        this.customHeaders.put(key.toLowerCase(), value);
        return this;
    }

    public Request build() {
        return new Request(host, scheme, path, port, params, customHeaders, body);
    }

}
