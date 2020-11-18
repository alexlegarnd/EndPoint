package ovh.alexisdelhaie.endpoint;

import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;
import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Application {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ConfigurationProperties props = new ConfigurationProperties();
        UIManager.setLookAndFeel(Tools.getLookAndFeel(props.getStringProperty("theme", "IntelliJ")));
        MainWindow dialog = new MainWindow(props);
        dialog.pack();
        dialog.setTitle("EndPoint");
        dialog.setVisible(true);
        Tools.centerFrame(dialog);
    }

}
