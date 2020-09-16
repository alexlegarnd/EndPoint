package ovh.alexisdelhaie.endpoint.http;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class HttpClient {

    public final static String CRLF = "\r\n";
    public final static int DEFAULT_TIMEOUT = 10000;

    private boolean allowInvalidSsl;

    public HttpClient() { this(false); }
    public HttpClient(boolean allowInvalidSsl) { this.allowInvalidSsl = allowInvalidSsl; }

    public Optional<Response> get(Request r) throws IOException, KeyManagementException, NoSuchAlgorithmException  {
        return process("GET", r, "");
    }

    public Optional<Response> post(Request r, String body) throws IOException, KeyManagementException, NoSuchAlgorithmException  {
        if (!r.getCustomHeaders().containsKey("content-length")) {
            r.getCustomHeaders().put("content-length", String.valueOf(body.length()));
        }
        return process("POST", r, body);
    }

    public Optional<Response> put(Request r, String body) throws IOException, KeyManagementException, NoSuchAlgorithmException  {
        if (!r.getCustomHeaders().containsKey("content-length")) {
            r.getCustomHeaders().put("content-length", String.valueOf(body.length()));
        }
        return process("PUT", r, body);
    }

    public Optional<Response> delete(Request r) throws IOException, KeyManagementException, NoSuchAlgorithmException  {
        return process("DELETE", r, "");
    }

    public Optional<Response> head(Request r) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return process("HEAD", r, "");
    }

    private Optional<Response> process(String method, Request r, String body) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String headers = buildHeaders(method, r);
        Socket s = (r.getScheme().equals("https")) ?
                buildSSLSocket(resolve(r.getHost()).getHostAddress(), r.getPort())
                : buildSocket(resolve(r.getHost()).getHostAddress(), r.getPort());
        String request = (method.equals("POST") || method.equals("PUT")) ?
                new StringBuilder(headers).append(body).toString() : headers;
        if (s.isConnected()) {
            Instant start = Instant.now();
            try {
                BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
                bos.write(request.getBytes(StandardCharsets.UTF_8));
                bos.flush();
                BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
                byte[] b = bis.readAllBytes();
                Instant end = Instant.now();
                long time = end.toEpochMilli() - start.toEpochMilli();
                r.setRawRequest(request);
                return Optional.of(new Response(b, time, r));
            } finally {
                s.close();
            }
        }
        return Optional.empty();
    }

    private InetAddress resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        return addresses[0];
    }

    private Socket buildSocket(String host, int port) throws IOException {
        Socket s = new Socket(host, port);
        s.setKeepAlive(false);
        s.setSoTimeout(DEFAULT_TIMEOUT);
        return s;
    }

    private Socket buildSSLSocket(String host, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        SSLSocketFactory factory;
        if(!allowInvalidSsl) {
            factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        } else {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){}
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            factory = sc.getSocketFactory();
        }
        Socket s =  factory.createSocket(host, port);
        ((SSLSocket)s).setEnabledProtocols(new String[] { "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" });
        s.setKeepAlive(false);
        s.setSoTimeout(DEFAULT_TIMEOUT);
        ((SSLSocket)s).startHandshake();
        return s;
    }

    private String buildHeaders(String method, Request r) {
        Map<String, String> custom = r.getCustomHeaders();
        String path = (r.getParams().isEmpty()) ?
                r.getPath() : r.getPathWithParams();
        final StringBuilder sb = new StringBuilder(method).append(" ").append(path)
                .append(" HTTP/1.1").append(CRLF);
        sb.append("host: ").append(r.getHost()).append(":").append(r.getPort()).append(CRLF);
        sb.append("connection: close").append(CRLF);
        if (!custom.containsKey("accept")) {
            sb.append("accept: */*").append(CRLF);
        }
        if (!custom.containsKey("user-agent")) {
            sb.append("user-agent: endpoint/1.0").append(CRLF);
        }
        for (Map.Entry<String, String> h : custom.entrySet()) {
            sb.append(h.getKey()).append(": ").append(h.getValue()).append(CRLF);
        }
        return sb.append(CRLF).toString();
    }

}
