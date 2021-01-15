package ovh.alexisdelhaie.endpoint.http;

import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.utils.MessageDialog;

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
    public final static boolean DEFAULT_ALLOW_INVALID_SSL = false;
    public final static boolean DEFAULT_ALLOW_DOWNGRADE = true;
    public final static String DEFAULT_HTTP_VERSION = "HTTP/1.0";

    private final boolean allowInvalidSsl;
    private final boolean allowDowngrade;
    private final String httpVersion;
    private final int timeout;

    private boolean downgraded;

    public HttpClient(ConfigurationProperties props) {
        this.allowInvalidSsl = props.getBooleanProperty("allowInvalidSsl", DEFAULT_ALLOW_INVALID_SSL);
        this.allowDowngrade = props.getBooleanProperty("allowDowngrade", DEFAULT_ALLOW_DOWNGRADE);
        this.httpVersion = props.getStringProperty("httpVersion", DEFAULT_HTTP_VERSION);
        this.timeout = props.getIntegerProperty("timeout", DEFAULT_TIMEOUT);

        this.downgraded = false;
    }

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
        Socket s = (r.getScheme().equals("https")) ?
                buildSSLSocket(resolve(r.getHost()).getHostAddress(), r.getPort())
                : buildSocket(resolve(r.getHost()).getHostAddress(), r.getPort());
        if (allowDowngrade && (s.getPort() != r.getPort())) {
            r.setPort(s.getPort());
        }
        String headers = buildHeaders(method, r);
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
                return Optional.of(new Response(b, time, downgraded, r));
            } catch (Exception e) {
                MessageDialog.error("HTTP Error", e.getMessage());
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
        s.setSoTimeout(timeout);
        return s;
    }

    private Socket buildSSLSocket(String host, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        System.setProperty("com.sun.net.ssl.rsaPreMasterSecretFix", "true");
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

        SSLSocket s =  (SSLSocket) factory.createSocket(host, port);
        s.setKeepAlive(false);
        s.setSoTimeout(timeout);
        if (allowDowngrade) {
            try {
                s.startHandshake();
                return s;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.out.println("Downgrade to Non-SSL socket");
                downgraded = true;
                return buildSocket(host, (port == 443) ? 80 : port);
            }
        }
        s.startHandshake();
        return s;
    }

    private String buildHeaders(String method, Request r) {
        Map<String, String> custom = r.getCustomHeaders();
        String path = (r.getParams().isEmpty()) ?
                r.getPath() : r.getPathWithParams();
        final StringBuilder sb = new StringBuilder(method).append(" ").append(path)
                .append(" ").append(httpVersion).append(CRLF);
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
