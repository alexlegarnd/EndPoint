package ovh.alexisdelhaie.endpoint;

import ovh.alexisdelhaie.endpoint.builder.TabBuilder;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.http.HttpClient;
import ovh.alexisdelhaie.endpoint.http.Request;
import ovh.alexisdelhaie.endpoint.http.RequestBuilder;
import ovh.alexisdelhaie.endpoint.http.Response;
import ovh.alexisdelhaie.endpoint.utils.MessageDialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

public class MainWindow extends JFrame {

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

    private final ConfigurationProperties props;
    private final HashMap<Integer, String> urls;

    public MainWindow() {
        props = new ConfigurationProperties();
        urls = new HashMap<>();
        setContentPane(contentPane);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TabBuilder.create(tabbedPane1, "New request", urls);
        Component tab = tabbedPane1.getSelectedComponent();
        urls.put(tab.hashCode(), "");
        newTabButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TabBuilder.create(tabbedPane1, "New request", urls);
                Component tab = tabbedPane1.getSelectedComponent();
                urls.put(tab.hashCode(), "");
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
        tabbedPane1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane1.getSelectedIndex() != -1) {
                    urlField.setText(urls.get(tabbedPane1.getSelectedComponent().hashCode()));
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
                    urls.put(tabbedPane1.getSelectedComponent().hashCode(), urlField.getText());
                }
            }
        });
    }

    private void sendRequest() {
        Optional<JSplitPane> possibleTab = getSelectedTab();
        if (possibleTab.isPresent()) {
            JSplitPane tab = possibleTab.get();
            int i = tabbedPane1.indexOfComponent(tab);
            JTextArea bodyField = TabBuilder.getResponseArea(i);
            progressBar1.setVisible(true);
            new Thread(() -> {
                try {
                    String url = urlField.getText();
                    HttpClient h = new HttpClient(props);
                    Request r = new RequestBuilder(url)
                            .build();
                    Optional<Response> possibleRes = Optional.empty();
                    switch ((String) Objects.requireNonNull(methodBox.getSelectedItem())) {
                        case "GET" -> possibleRes = h.get(r);
                        case "POST" -> possibleRes = h.post(r, "");
                        case "PUT" -> possibleRes = h.put(r, "");
                        case "DELETE" -> possibleRes = h.delete(r);
                        case "HEAD" -> possibleRes = h.head(r);
                    }
                    if (possibleRes.isPresent()) {
                        Response res = possibleRes.get();

                        bodyField.setText(res.getBody());
                    }
                } catch (KeyManagementException | IOException | NoSuchAlgorithmException e) {
                    bodyField.setText(e.getMessage());
                } finally {
                    progressBar1.setVisible(false);
                }
            }).start();
        } else {
            MessageDialog.error("Error", "Cannot get current tab");
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
