package ovh.alexisdelhaie.endpoint;

import ovh.alexisdelhaie.endpoint.builder.TabBuilder;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationDialog;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.http.HttpClient;
import ovh.alexisdelhaie.endpoint.http.Request;
import ovh.alexisdelhaie.endpoint.http.RequestBuilder;
import ovh.alexisdelhaie.endpoint.http.Response;
import ovh.alexisdelhaie.endpoint.utils.MessageDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    private final HashMap<Integer, String> urls;

    private final ConcurrentHashMap<Integer, Boolean> controlState;
    private final ConcurrentHashMap<Integer, Response> responses;

    public MainWindow() throws IOException {
        props = new ConfigurationProperties();
        controlState = new ConcurrentHashMap<>();
        responses = new ConcurrentHashMap<>();
        urls = new HashMap<>();
        setIconImage(ImageIO.read(MainWindow.class.getResource("/icon.png")));
        setContentPane(contentPane);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TabBuilder.create(tabbedPane1, "New request", urls, urlField);
        Component tab = tabbedPane1.getSelectedComponent();
        urls.put(tab.hashCode(), "");
        enableControl(true, tab.hashCode());
        settingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ConfigurationDialog.showDialog(props);
            }
        });
        newTabButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TabBuilder.create(tabbedPane1, "New request", urls, urlField);
                Component tab = tabbedPane1.getSelectedComponent();
                urls.put(tab.hashCode(), "");
                enableControl(true, tab.hashCode());
                showStatus(tab.hashCode());
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
                urlField.setText(urls.get(hashCode));
                enableControl(controlState.get(hashCode), hashCode);
                showStatus(tab.hashCode());
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
                    urls.put(tabbedPane1.getSelectedComponent().hashCode(), urlField.getText());
                }
            }
        });
    }

    private void sendRequest() {
        Optional<JSplitPane> possibleTab = getSelectedTab();
        if (possibleTab.isPresent()) {
            JSplitPane tab = possibleTab.get();
            int tabHashCode = tab.hashCode();
            statusLabel.setVisible(false);
            enableControl(false, tabHashCode);
            int i = tabbedPane1.indexOfComponent(tab);
            JTextArea responseBody = TabBuilder.getResponseArea(i);
            responseBody.setForeground(Color.black);
            responseBody.setText("");
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
                        responses.put(tabHashCode, res);
                        responseBody.setText(res.getBody());
                    }
                } catch (KeyManagementException | IOException | NoSuchAlgorithmException e) {
                    responseBody.setForeground(Color.red);
                    responseBody.setText(e.getMessage());
                    if (responses.containsKey(tabHashCode)) {
                        responses.remove(tabHashCode);
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
            controlState.put(hashCode, state);
            if (tabbedPane1.getSelectedComponent().hashCode() == hashCode) {
                sendButton.setEnabled(state);
                urlField.setEnabled(state);
                methodBox.setEnabled(state);
                progressBar1.setIndeterminate(!state);
            }
        }
    }

    private void showStatus(int hashCode) {
        if (controlState.get(hashCode) && responses.containsKey(hashCode) &&
                tabbedPane1.getSelectedComponent().hashCode() == hashCode) {
            statusLabel.setForeground(Color.BLACK);
            Response res = responses.get(hashCode);
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

}
