package ovh.alexisdelhaie.endpoint.configuration;

import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    public static final int WIDTH = 450;
    public static final int HEIGHT = 500;

    public static final String VERSION = "0.1.3";

    private JPanel contentPane;
    private JLabel version;
    private JLabel javaVersion;

    private AboutDialog() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("About EndPoint");
        version.setText("Version: " + VERSION + " (NOT FINISHED)");
        javaVersion.setText("Software: Java " + System.getProperty("java.version") + " (GUI: Java Swing)");
    }

    public static void showDialog() {
        AboutDialog dialog = new AboutDialog();
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setResizable(false);
        Tools.centerFrame(dialog);
        dialog.setVisible(true);
    }

}
