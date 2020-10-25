package ovh.alexisdelhaie.endpoint.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class URLGenerator {

    public static final String HTTP_START = "http://";
    public static final String HTTPS_START = "https://";

    public static String processNewUrl(HashMap<String, String> p, String url) {
        try {
            if (!url.startsWith(HTTP_START) && !url.startsWith(HTTPS_START)) {
                url = HTTP_START + url;
            }
            String newUrl = (url.startsWith(HTTPS_START)) ? HTTPS_START : HTTP_START;
            URL u = new URL(url);
            newUrl += u.getHost();
            if (u.getPort() != -1) {
                newUrl += ":" + u.getPort();
            }
            newUrl += (u.getPath().isBlank()) ? "/" : u.getPath();
            newUrl += generateParamsPart(p);
            return newUrl;
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
        }
        return url;
    }

    public static String generateParamsPartWithEncoding(HashMap<String, String> p) {
        String result = "?";
        for (Map.Entry<String, String> entry : p.entrySet()) {
            result = new StringBuilder(result)
                    .append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&").toString();
        }
        if (result.equals("?")) {
            result = "";
        } else if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String generateParamsPart(HashMap<String, String> p) {
        String result = "?";
        for (Map.Entry<String, String> entry : p.entrySet()) {
            String key = SpecialChar.encodeString(entry.getKey());
            String value = SpecialChar.encodeString(entry.getValue());
            result = new StringBuilder(result)
                    .append(key)
                    .append("=")
                    .append(value)
                    .append("&").toString();
        }
        if (result.equals("?")) {
            result = "";
        } else if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String addSchemaToUrl(String url) {
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = "http://" + url;
        }
        return url;
    }

}
