package ovh.alexisdelhaie.endpoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class URLGenerator {

    public static final String HTTP_START = "http://";
    public static final String HTTPS_START = "https://";
    public static final String[][] SPECIAL_CHARS = new String[][] {
        { " ", "%20" }, { "<", "%3C" }, { ">", "%3E" },
        { "#", "%23" }, { "{", "%7B" },
        { "}", "%7D" }, { "|", "%7C" }, { "\\", "%5C" },
        { "^", "%5E" }, { "~", "%7E" }, { "[", "%5B" },
        { "]", "%5D" }, { "`", "%60" }, { ";", "%3B" },
        { "/", "%2F" }, { "?", "%3F" }, { ":", "%3A" },
        { "@", "%40" }, { "=", "%3D" }, { "&", "%26" },
        { "$", "%24" }, { "\r", "%0D" }, { "\n", "%0A" }
    };

    static public String processNewUrl(HashMap<String, String> p, String url) {
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

    public static Map<String, String> getSpecialCharRef() {
        return Stream.of(SPECIAL_CHARS)
                .collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));
    }

    static public String generateParamsPartWithEncoding(HashMap<String, String> p) {
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

    static public String generateParamsPart(HashMap<String, String> p) {
        Map<String, String> specialCharRef = getSpecialCharRef();

        String result = "?";
        for (Map.Entry<String, String> entry : p.entrySet()) {
            String key = escapeText(entry.getKey(), specialCharRef);
            String value = escapeText(entry.getValue(), specialCharRef);
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

    static public String escapeText(String s, Map<String, String> scr) {
        String result = s.replace("%", "%25");
        for (Map.Entry<String, String> entry : scr.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

}
