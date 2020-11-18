package ovh.alexisdelhaie.endpoint;

import ovh.alexisdelhaie.endpoint.builder.TabBuilder;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationDialog;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.http.HttpClient;
import ovh.alexisdelhaie.endpoint.http.Request;
import ovh.alexisdelhaie.endpoint.http.RequestBuilder;
import ovh.alexisdelhaie.endpoint.http.Response;
import ovh.alexisdelhaie.endpoint.utils.MessageDialog;
import ovh.alexisdelhaie.endpoint.utils.RequestTab;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindow extends JFrame {

    private enum StatusColor {
        INFORMATION("#53baf5"),
        SUCCESS("#7ccf16"),
        REDIRECTION("#b153f5"),
        ERROR_CLIENT("#f5ca53"),
        ERROR_SERVER("#f55353");

        private final String hex;

        StatusColor(String s) {
            hex = s;
        }

        public String getHex() {
            return hex;
        }
    }

    // Constants
    public final static int WIDTH = 1280;
    public final static int HEIGHT = 720;
    public final static String NEW_TAB_NAME = "New request";

    private JPanel contentPane;
    private JComboBox<String> methodBox;
    private JTextField urlField;
    private JButton sendButton;
    private JTabbedPane tabbedPane1;
    private JButton newTabButton;
    private JProgressBar progressBar1;
    private JLabel statusLabel;
    private JButton settingsButton;

    private final ConfigurationProperties props;
    private final ConcurrentHashMap<Integer, RequestTab> tabs;

    public MainWindow(ConfigurationProperties props) throws IOException {
        this.props = props;
        tabs = new ConcurrentHashMap<>();
        setIconImage(ImageIO.read(MainWindow.class.getResource("/icon.png")));
        setContentPane(contentPane);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newTab();
        settingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ConfigurationDialog.showDialog(props, MainWindow.this);
            }
        });
        newTabButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newTab();
            }
        });
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!urlField.getText().isBlank()) {
                    sendRequest();
                } else {
                    MessageDialog.info("Url field empty", "Please enter an url");
                }
            }
        });
        tabbedPane1.addChangeListener(e -> {
            if (tabbedPane1.getSelectedIndex() != -1) {
                int hashCode = tabbedPane1.getSelectedComponent().hashCode();
                RequestTab requestTab = tabs.get(hashCode);
                if (Objects.nonNull(requestTab)) {
                    urlField.setText(requestTab.getUrl());
                    methodBox.setSelectedItem(requestTab.getMethod());
                    enableControl(requestTab.isRunning(), hashCode);
                    showStatus(hashCode);
                }
            }
        });
        methodBox.addItemListener((e) -> {
            if (tabbedPane1.getSelectedIndex() != -1) {
                int hashCode = tabbedPane1.getSelectedComponent().hashCode();
                RequestTab requestTab = tabs.get(hashCode);
                if (Objects.nonNull(requestTab)) {
                    requestTab.setMethod((String) methodBox.getSelectedItem());
                }
            }
        });
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }
            public void removeUpdate(DocumentEvent e) {
                warn();
            }
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                if (tabbedPane1.getSelectedIndex() != -1) {
                    int i = tabbedPane1.indexOfComponent(tabbedPane1.getSelectedComponent());
                    JLabel title = TabBuilder.getLabel(i);
                    tabs.get(tabbedPane1.getSelectedComponent().hashCode()).setUrl(urlField.getText());
                    if (Objects.nonNull(title)) {
                        if (!urlField.getText().isBlank()) {
                            try {
                                URL u = new URL((!urlField.getText().toLowerCase().startsWith("http://") &&
                                        !urlField.getText().toLowerCase().startsWith("https://")) ?
                                        "http://" + urlField.getText() : urlField.getText());
                                parseParamsFromUrl(urlField.getText());
                                if (u.getPath().isBlank()) {
                                    title.setText(u.getHost());
                                } else {
                                    title.setText(String.format("%s (%s)", u.getPath(), u.getHost()));
                                }
                            } catch (MalformedURLException e) {
                                title.setText(urlField.getText());
                            }
                        } else {
                            title.setText(NEW_TAB_NAME);
                        }
                    }
                }
            }
        });
    }

    private void newTab() {
        RequestTab requestTab = new RequestTab((String) methodBox.getSelectedItem());
        TabBuilder.create(tabbedPane1, NEW_TAB_NAME, tabs, urlField);
        Component tab = tabbedPane1.getSelectedComponent();
        tabs.put(tab.hashCode(), requestTab);
        enableControl(true, tab.hashCode());
        showStatus(tab.hashCode());
        urlField.setText("");
    }

    private void sendRequest() {
        Optional<JSplitPane> possibleTab = getSelectedTab();
        if (possibleTab.isPresent()) {
            JSplitPane tab = possibleTab.get();
            int tabHashCode = tab.hashCode();
            RequestTab requestTab = tabs.get(tabHashCode);
            statusLabel.setVisible(false);
            enableControl(false, tabHashCode);
            int i = tabbedPane1.indexOfComponent(tab);
            JTextArea responseBody = TabBuilder.getResponseArea(i);
            JTextArea responseHeader = TabBuilder.getResponseHeaderTextArea(i);
            JTextArea requestHeader = TabBuilder.getRequestHeaderTextArea(i);
            responseBody.setForeground(Color.black);
            responseBody.setText("");
            responseHeader.setText("");
            requestHeader.setText("");
            JTextArea bodyField = TabBuilder.getBody(i);
            new Thread(() -> {
                try {
                    String url = urlField.getText();
                    HttpClient h = new HttpClient(props);
                    Request r = new RequestBuilder(url)
                            .setCustomHeaders(TabBuilder.getHeaders(i))
                            .build();
                    Optional<Response> possibleRes = Optional.empty();
                    switch ((String) Objects.requireNonNull(methodBox.getSelectedItem())) {
                        case "GET" -> possibleRes = h.get(r);
                        case "POST" -> possibleRes = h.post(r, bodyField.getText());
                        case "PUT" -> possibleRes = h.put(r, bodyField.getText());
                        case "DELETE" -> possibleRes = h.delete(r);
                        case "HEAD" -> possibleRes = h.head(r);
                    }
                    if (possibleRes.isPresent()) {
                        Response res = possibleRes.get();
                        requestTab.setRes(res);
                        responseBody.setText(res.getBody());
                        requestHeader.setText(res.getRequest().getRawRequest());
                        responseHeader.setText(res.getRawHeaders());
                    }
                } catch (KeyManagementException | IOException | NoSuchAlgorithmException e) {
                    responseBody.setForeground(Color.red);
                    responseBody.setText(e.getMessage());
                    if (Objects.nonNull(requestTab.getRes())) {
                        requestTab.setRes(null);
                        showStatus(tabHashCode);
                    }
                } finally {
                    enableControl(true, tabHashCode);
                    showStatus(tabHashCode);
                }
            }).start();
        } else {
            MessageDialog.error("Error", "Cannot get current tab");
        }
    }

    private void enableControl(Boolean state, int hashCode) {
        if (Objects.nonNull(state)) {
            tabs.get(hashCode).setRunning(state);
            if (tabbedPane1.getSelectedComponent().hashCode() == hashCode) {
                sendButton.setEnabled(state);
                urlField.setEnabled(state);
                methodBox.setEnabled(state);
                progressBar1.setIndeterminate(!state);
            }
        }
    }

    private void showStatus(int hashCode) {
        RequestTab requestTab = tabs.get(hashCode);
        if (requestTab.isRunning() && Objects.nonNull(requestTab.getRes()) &&
                tabbedPane1.getSelectedComponent().hashCode() == hashCode) {
            statusLabel.setForeground(Color.BLACK);
            Response res = requestTab.getRes();
            final StringBuilder sb = new StringBuilder();
            if (res.getStatusCode() != -1) {
                sb.append(res.getStatusCode())
                        .append(" ")
                        .append(res.getStatus())
                        .append(" (in ")
                        .append(res.getTime())
                        .append(" ms)");
                if (res.getStatusCode() >= 100 && res.getStatusCode() < 200) {
                    statusLabel.setForeground(Color.decode(StatusColor.INFORMATION.getHex()));
                } else if (res.getStatusCode() >= 200 && res.getStatusCode() < 300) {
                    statusLabel.setForeground(Color.decode(StatusColor.SUCCESS.getHex()));
                } else if (res.getStatusCode() >= 300 && res.getStatusCode() < 400) {
                    statusLabel.setForeground(Color.decode(StatusColor.REDIRECTION.getHex()));
                } else if (res.getStatusCode() >= 400 && res.getStatusCode() < 500) {
                    statusLabel.setForeground(Color.decode(StatusColor.ERROR_CLIENT.getHex()));
                } else if (res.getStatusCode() >= 500) {
                    statusLabel.setForeground(Color.decode(StatusColor.ERROR_SERVER.getHex()));
                }
            } else {
                sb.append("in ")
                .append(res.getTime())
                .append(" ms");
            }

            statusLabel.setText(sb.toString());
            statusLabel.setVisible(true);
        } else {
            statusLabel.setVisible(false);
        }
    }

    private Optional<JSplitPane> getSelectedTab() {
        Component c = tabbedPane1.getSelectedComponent();
        if (c instanceof  JSplitPane) {
            return Optional.of((JSplitPane) c);
        }
        return Optional.empty();
    }

    private void parseParamsFromUrl(String url) {
        Optional<JSplitPane> possibleTab = getSelectedTab();
        if (possibleTab.isPresent()) {
            int id = tabbedPane1.indexOfComponent(possibleTab.get());
            JTable table = TabBuilder.getParamsTable(id);
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            for (int i = m.getRowCount() - 1; i > -1; i--) {
                m.removeRow(i);
            }
            Pattern pattern = Pattern.compile("[^&?]*?=[^&?]*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                String param = matcher.group();
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    m.addRow(new Object[]{kv[0], kv[1]});
                }
            }
        }
    }

}
