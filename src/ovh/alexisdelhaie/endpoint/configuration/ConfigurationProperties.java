package ovh.alexisdelhaie.endpoint.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationProperties {

    private Map<String, String> properties;
    private String osname;
    private String filepath;
    private ObjectMapper mapper;

    @SuppressWarnings("unchecked")
    public ConfigurationProperties() {
        osname = System.getProperty("os.name").toUpperCase();
        properties = new HashMap<>();
        mapper = new ObjectMapper();
        filepath = new StringBuilder(getAppData())
                .append("EndPoint")
                .append(getSeparator())
                .append("settings.json")
                .toString();
        createAppFolder();
        load();
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        save();
    }

    public String getStringProperty(String key, String defaultS) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return defaultS;
    }

    public boolean getBooleanProperty(String key, boolean defaultB) {
        if (properties.containsKey(key)) {
            return Boolean.parseBoolean(properties.get(key));
        }
        return defaultB;
    }

    private void save() {
        try {
            mapper.writeValue(new File(filepath), properties);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot save settings");
            alert.setHeaderText("There was an error while saving settings file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void load() {
        File f = new File(filepath);
        try {
            if (f.exists()) {
                properties = mapper.readValue(f, new TypeReference<Map<String, String>>() { });
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot initialize settings");
            alert.setHeaderText("There was an error while initializing settings file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void createAppFolder() {
        try {
            Path path = Paths.get(new StringBuilder(getAppData())
                    .append("EndPoint")
                    .append(getSeparator()).toString());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot create app folder");
            alert.setHeaderText("There was an error while creating appdata folder");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private String getAppData() {
        String path = "";
        if (osname.contains("WIN")) {
            path = System.getenv("APPDATA");
            path = (path.endsWith("\\") ? path : path + "\\");
        }
        else if (osname.contains("MAC")) {
            path = System.getProperty("user.home") + "/Library/";
        }
        else if (osname.contains("NUX")) {
            path = System.getProperty("user.home");
            path = (path.endsWith("/") ? path : path + "/");
        }
        else {
            path = System.getProperty("user.dir");
            path = (path.endsWith("/") ? path : path + "/");
        }

        return path;
    }

    private String getSeparator() {
        if (osname.contains("WIN")) {
            return "\\";
        }
        return "/";
    }

}
