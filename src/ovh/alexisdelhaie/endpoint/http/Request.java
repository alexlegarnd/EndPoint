package ovh.alexisdelhaie.endpoint.http;

import ovh.alexisdelhaie.endpoint.URLGenerator;

import java.util.HashMap;
import java.util.Objects;

public class Request {

    private String host;
    private String scheme;
    private String path;
    private int port;
    private HashMap<String, String> params;
    private HashMap<String, String> customHeaders;
    private String body;
    private String rawRequest;

    Request(String host, String scheme, String path, int port,
                   HashMap<String, String> params, HashMap<String, String> customHeaders, String body) {
        this.host = host;
        this.scheme = scheme;
        this.path = path;
        this.port = port;
        this.params = params;
        this.customHeaders = customHeaders;
        this.body = body;
    }

    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithParams() {
        return String.format("%s%s", path, URLGenerator.generateParamsPartWithEncoding(params));
    }

    public int getPort() {
        return port;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public HashMap<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public String getBody() {
        return body;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    void setRawRequest(String r) {
        rawRequest = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return getPort() == request.getPort() &&
                Objects.equals(getHost(), request.getHost()) &&
                Objects.equals(getScheme(), request.getScheme()) &&
                Objects.equals(getPath(), request.getPath()) &&
                Objects.equals(getParams(), request.getParams()) &&
                Objects.equals(getCustomHeaders(), request.getCustomHeaders()) &&
                Objects.equals(getBody(), request.getBody());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getScheme(), getPath(), getPort(), getParams(), getCustomHeaders(), getBody());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Request{");
        sb.append("scheme='").append(scheme).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", path='").append(path).append('\'');
        sb.append(", params=").append(params);
        sb.append(", customHeaders=").append(customHeaders);
        sb.append(", body='").append(body).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
