package ovh.alexisdelhaie.endpoint.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ovh.alexisdelhaie.endpoint.configuration.ConfigurationProperties;

import java.io.IOException;

public class ConfigurationController
{

    private ConfigurationProperties configurationProperties;
    private Stage primaryStage;

    @FXML
    private CheckBox allowInvalidSsl;
    @FXML
    private CheckBox allowDowngrade;
    @FXML
    private ChoiceBox<String> httpVersion;

    public void setStageAndSetupListeners(Stage s) {
        primaryStage = s;
    }

    public void setConfigurationProperties(ConfigurationProperties properties) {
        String[] versions = { "HTTP/1.1", "HTTP/1.0" };
        httpVersion.setItems(FXCollections.observableArrayList(versions));
        configurationProperties = properties;
        allowInvalidSsl.setSelected(properties.getBooleanProperty("allowInvalidSsl", false));
        allowDowngrade.setSelected(properties.getBooleanProperty("allowDowngrade", true));
        httpVersion.setValue(properties.getStringProperty("httpVersion", versions[0]));
        httpVersion.setOnAction(this::onChoiceBoxValueChanged);
    }

    @FXML
    private void onBooleanValueChanged(MouseEvent event) {
        CheckBox c = (CheckBox) event.getSource();
        configurationProperties.setProperty(c.getId(), String.valueOf(c.isSelected()));
    }

    @SuppressWarnings("unchecked")
    private void onChoiceBoxValueChanged(ActionEvent event) {
        ChoiceBox<String> c = (ChoiceBox<String>) event.getSource();
        configurationProperties.setProperty(c.getId(), c.getValue());
    }

    @FXML
    private void showAboutDialog() {
        try {
            Stage dialog = new Stage();
            Parent xml = FXMLLoader.load(getClass().getResource("about.fxml"));
            dialog.initOwner(primaryStage);
            dialog.setScene(new Scene(xml, 707, 365));
            dialog.setMaxHeight(365);
            dialog.setMinHeight(365);
            dialog.setMaxWidth(707);
            dialog.setMinWidth(707);
            dialog.setResizable(false);
            dialog.setTitle("About EndPoint");
            dialog.getIcons().add( new Image(
                    Controller.class.getResourceAsStream( "icon.png" )));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Cannot initialize About");
            alert.setHeaderText("There was an error while initializing this dialog");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

}
