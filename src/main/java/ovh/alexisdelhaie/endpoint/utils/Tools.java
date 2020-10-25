package ovh.alexisdelhaie.endpoint.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;

public class Tools {

    public static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    public static void centerFrame(Dialog dialog) {
        int y = (int)( screen.getHeight() / 2 ) - dialog.getHeight() / 2;
        int x = (int)( screen.getWidth() / 2 ) - dialog.getWidth() / 2;
        dialog.setLocation(x, y);
    }

    public static void centerFrame(Frame frame) {
        int y = (int)( screen.getHeight() / 2 ) - frame.getHeight() / 2;
        int x = (int)( screen.getWidth() / 2 ) - frame.getWidth() / 2;
        frame.setLocation(x, y);
    }

    public static HashMap<String, String> tableToHashMap(JTable table) {
        HashMap<String, String> result = new HashMap<>();
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            String key = (String) m.getValueAt(i, 0);
            String value = (String) m.getValueAt(i, 1);
            result.put(key, value);
        }
        return result;
    }

}
