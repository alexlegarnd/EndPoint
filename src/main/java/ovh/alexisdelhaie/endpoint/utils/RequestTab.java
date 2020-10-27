package ovh.alexisdelhaie.endpoint.utils;

import ovh.alexisdelhaie.endpoint.http.Response;

public class RequestTab {

    private String url;
    private boolean running;
    private Response res;
    private String method;

    public RequestTab(String method) {
        url = "";
        running = false;
        res = null;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public RequestTab setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isRunning() {
        return running;
    }

    public RequestTab setRunning(boolean running) {
        this.running = running;
        return this;
    }

    public Response getRes() {
        return res;
    }

    public RequestTab setRes(Response res) {
        this.res = res;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RequestTab setMethod(String method) {
        this.method = method;
        return this;
    }

}
