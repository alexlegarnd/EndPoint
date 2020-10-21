package ovh.alexisdelhaie.endpoint.utils;

import javax.swing.*;

public class MessageDialog {

    public static void error(String title, String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, title,
                JOptionPane.ERROR_MESSAGE);
    }

    public static void warning(String title, String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, title,
                JOptionPane.WARNING_MESSAGE);
    }

    public static void info(String title, String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

}
