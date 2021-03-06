package ovh.alexisdelhaie.endpoint.http;

import ovh.alexisdelhaie.endpoint.http.parsers.Chunked;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {

    public final static String CRLF = "\r\n";
    public final static String DOUBLE_CRLF = "\r\n\r\n";

    private final HashMap<String, String> headers;
    private final String rawHeaders;
    private String rawResponse;
    private String body;
    private int statusCode;
    private String status;
    private final long time;
    private final Request request;
    private final boolean downgraded;

    Response(byte[] res, long time, boolean downgraded, Request r) throws UnsupportedEncodingException {
        headers = new HashMap<>();
        rawResponse = new String(res, StandardCharsets.UTF_8);
        int crlf = rawResponse.indexOf(DOUBLE_CRLF);
        if (crlf > 0) {
            rawHeaders = rawResponse.substring(0, crlf);
            parseHeaders();
            rawResponse = new String(res, getEncoding());
            body = rawResponse.substring(crlf + DOUBLE_CRLF.length());
            parseBody();
        } else {
            rawHeaders = rawResponse;
        }
        this.time = time;
        request = r;
        this.downgraded = downgraded;
    }

    private void parseHeaders() {
        String[] hh = rawHeaders.split(CRLF);
        parseStatus(hh[0]);
        for (String c : hh) {
            String[] entry = c.split(":\\s");
            if (entry.length == 2) {
                headers.put(entry[0].toLowerCase(), entry[1]);
            }
        }
    }

    private void parseStatus(String l) {
        Pattern p = Pattern.compile("^(HTTP/[0-2].[0-1])\\s([0-9]{3})\\s(.+)$");
        Matcher m = p.matcher(l);
        if (m.matches()) {
            statusCode = Integer.parseInt(m.group(2));
            status = m.group(3);
        } else {
            p = Pattern.compile("^(HTTP/[0-2].[0-1])\\s([0-9]{3})?(.*)$");
            m = p.matcher(l);
            if (m.matches()) {
                statusCode = Integer.parseInt(m.group(2));
                HttpStatus httpStatus = HttpStatus.findByCode(statusCode);
                status = (httpStatus != null)  ? httpStatus.getMessage() : "";
            } else {
                statusCode = -1;
                status = "Cannot get status form HTTP Response";
            }
        }
    }

    private void parseBody() {
        if (headers.containsKey("transfer-encoding")) {
            if (headers.get("transfer-encoding").toLowerCase().contains("chunked")) {
                body = Chunked.parse(body);
            }
        }
    }

    private String getEncoding() {
        Pattern p = Pattern.compile("(.+);\\s(.+)=(.+)");
        if (headers.containsKey("content-type")) {
            String value = headers.get("content-type");
            Matcher m = p.matcher(value);
            if (m.matches()) {
                return m.group(3);
            }
        }
        return StandardCharsets.UTF_8.toString();
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getRawHeaders() {
        return rawHeaders;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }

    public long getTime() {
        return time;
    }

    public Request getRequest() { return request; }

    public boolean isDowngraded() {
        return downgraded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response)) return false;
        Response response = (Response) o;
        return statusCode == response.statusCode &&
                time == response.time &&
                headers.equals(response.headers) &&
                rawHeaders.equals(response.rawHeaders) &&
                rawResponse.equals(response.rawResponse) &&
                body.equals(response.body) &&
                status.equals(response.status) &&
                request.equals(response.request);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, rawHeaders, rawResponse, body, statusCode, status, time, request);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Response{");
        sb.append("headers=").append(headers);
        sb.append(", body='").append(body).append('\'');
        sb.append(", statusCode=").append(statusCode);
        sb.append(", status='").append(status).append('\'');
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}
