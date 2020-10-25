package ovh.alexisdelhaie.endpoint;

import com.formdev.flatlaf.FlatIntelliJLaf;
import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Application {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, IOException {
        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        MainWindow dialog = new MainWindow();
        dialog.pack();
        dialog.setTitle("EndPoint");
        dialog.setVisible(true);
        Tools.centerFrame(dialog);
    }

}
