package ovh.alexisdelhaie.endpoint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainwindow.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setStageAndSetupListeners(primaryStage);
        primaryStage.setTitle("EndPoint");
        primaryStage.setScene(new Scene(root, 1067, 644));
        primaryStage.setMinWidth(1067);
        primaryStage.setMinHeight(644);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add( new Image(
                Main.class.getResourceAsStream( "icon.png" )));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
