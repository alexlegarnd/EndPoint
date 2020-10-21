package ovh.alexisdelhaie.endpoint;

import ovh.alexisdelhaie.endpoint.builder.TabBuilder;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.http.HttpClient;
import ovh.alexisdelhaie.endpoint.http.Request;
import ovh.alexisdelhaie.endpoint.http.RequestBuilder;
import ovh.alexisdelhaie.endpoint.http.Response;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class MainWindow extends JFrame {

    // Constants
    public final static int WIDTH = 1280;
    public final static int HEIGHT = 720;

    private JPanel contentPane;
    private JComboBox<String> comboBox1;
    private JTextField textField1;
    private JButton sendButton;
    private JTabbedPane tabbedPane1;
    private JButton newTabButton;

    private ConfigurationProperties props;

    public MainWindow() {
        props = new ConfigurationProperties();
        setContentPane(contentPane);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TabBuilder.create(tabbedPane1, "New request");
        newTabButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TabBuilder.create(tabbedPane1, "New request");
            }
        });
        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    sendRequest();
                } catch (IOException | NoSuchAlgorithmException | KeyManagementException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void centerFrame() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int y = (int)( screen.getHeight() / 2 ) - this.getHeight() / 2;
        int x = (int)( screen.getWidth() / 2 ) - this.getWidth() / 2;
        this.setLocation(x, y);
    }

    private void sendRequest() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        Optional<JSplitPane> possibleTab = getSelectedTab();
        if (possibleTab.isPresent()) {
            JSplitPane tab = possibleTab.get();
            String url = textField1.getText();
            HttpClient h = new HttpClient(props);
            Request r = new RequestBuilder(url)
                    .build();
            Optional<Response> possibleRes = h.get(r);
            if (possibleRes.isPresent()) {
                Response res = possibleRes.get();
                int i = tabbedPane1.indexOfComponent(tab);
                JTextArea t = TabBuilder.getResponseArea(i);
                t.setText(res.getBody());
            }
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
