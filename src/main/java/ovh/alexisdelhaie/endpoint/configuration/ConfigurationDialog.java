package ovh.alexisdelhaie.endpoint.configuration;

import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.swing.*;
import java.awt.*;

public class ConfigurationDialog extends JDialog {

    public static final int WIDTH = 450;
    public static final int HEIGHT = 700;

    private JPanel contentPane;
    private JButton buttonOK;
    private JCheckBox allowInvalidSsl;
    private JCheckBox allowDowngrade;
    private JComboBox<String> httpVersion;
    private JSpinner timeout;
    private JButton aboutButton;

    private final ConfigurationProperties props;

    private ConfigurationDialog(ConfigurationProperties props) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Settings");
        this.props = props;
        buttonOK.addActionListener(e -> onOK());
        aboutButton.addActionListener(e -> AboutDialog.showDialog());
        allowInvalidSsl.setSelected(this.props.getBooleanProperty("allowInvalidSsl", false));
        allowDowngrade.setSelected(this.props.getBooleanProperty("allowDowngrade", true));
        httpVersion.setSelectedItem(this.props.getStringProperty("httpVersion", "HTTP/1.0"));
        timeout.setValue(this.props.getIntegerProperty("timeout", 10000));

        allowInvalidSsl.addActionListener((e) -> {
            this.props.setProperty("allowInvalidSsl", String.valueOf(allowInvalidSsl.isSelected()));
        });
        allowDowngrade.addActionListener((e) -> {
            this.props.setProperty("allowDowngrade", String.valueOf(allowDowngrade.isSelected()));
        });
        httpVersion.addActionListener((e) -> {
            this.props.setProperty("httpVersion", (String) httpVersion.getSelectedItem());
        });
        timeout.addChangeListener((e) -> {
            this.props.setProperty("timeout", String.valueOf(timeout.getValue()));
        });

    }

    private void onOK() {
        dispose();
    }

    public static void showDialog(ConfigurationProperties props) {
        ConfigurationDialog dialog = new ConfigurationDialog(props);
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setResizable(false);
        Tools.centerFrame(dialog);
        dialog.setVisible(true);
    }
}
