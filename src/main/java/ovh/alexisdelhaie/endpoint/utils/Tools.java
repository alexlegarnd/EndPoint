package ovh.alexisdelhaie.endpoint.utils;

import java.awt.*;

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

}
