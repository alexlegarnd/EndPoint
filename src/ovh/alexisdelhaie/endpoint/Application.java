package ovh.alexisdelhaie.endpoint;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

public class Application {

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatIntelliJLaf());
        MainWindow dialog = new MainWindow();
        dialog.pack();
        dialog.setTitle("EndPoint");
        dialog.setVisible(true);
        dialog.centerFrame();
    }

}
