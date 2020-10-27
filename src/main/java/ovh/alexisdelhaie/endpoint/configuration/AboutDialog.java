package ovh.alexisdelhaie.endpoint.configuration;

import ovh.alexisdelhaie.endpoint.MainWindow;
import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class AboutDialog extends JDialog {

    public static final int WIDTH = 740;
    public static final int HEIGHT = 500;

    public static final String VERSION = "0.1.4";

    private JPanel contentPane;
    private JLabel version;
    private JLabel javaVersion;
    private JLabel banner;

    private AboutDialog() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("About EndPoint");
        version.setText("Version: " + VERSION + " (NOT FINISHED)");
        javaVersion.setText("Software: Java " + System.getProperty("java.version") + " (GUI: Java Swing)");
        try {
            banner.setIcon(new ImageIcon(ImageIO.read(MainWindow.class.getResource("/banner.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
