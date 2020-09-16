package ovh.alexisdelhaie.endpoint;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ovh.alexisdelhaie.endpoint.http.HttpClient;
import ovh.alexisdelhaie.endpoint.http.Request;
import ovh.alexisdelhaie.endpoint.http.RequestBuilder;
import ovh.alexisdelhaie.endpoint.http.Response;
import ovh.alexisdelhaie.endpoint.impl.EditCell;
import ovh.alexisdelhaie.endpoint.model.Param;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public class Controller implements Initializable {

    private enum StatusColor {
        INFORMATION("#53baf5"),
        SUCCESS("#7ccf16"),
        REDIRECTION("#b153f5"),
        ERROR_CLIENT("#f5ca53"),
        ERROR_SERVER("#f55353"),
        DEFAULT("BLACK");

        private final String hex;

        StatusColor(String s) {
            hex = s;
        }

        public String getHex() {
            return hex;
        }
    }

    private enum RequestTab {
        PARAMS(0),
        AUTHORIZATION(1),
        HEADERS(2),
        BODY(3),
        RESPONSE(4);

        private final int index;

        RequestTab (int i) { index = i; }
        public int getIndex() { return index; }
    }

    @FXML
    private ChoiceBox<String> httpMethod;
    @FXML
    private TabPane tabs;
    @FXML
    private TextField requestInput;
    @FXML
    private Pane runningIndicatorPane;

    private Stage primaryStage;
    private HashMap<Integer, String> requests;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requests = new HashMap<>();
        String[] method = { "GET", "POST", "HEAD", "PUT", "DELETE" };
        httpMethod.setItems(FXCollections.observableArrayList(method));
        httpMethod.setValue(method[0]);
        tabs.getSelectionModel().selectedItemProperty().addListener(
                (ov, t, t1) -> requestInput.setText((t1 != null) ? requests.get(t1.hashCode()) : "")
        );
        createNewTab();
    }

    private Tab newTab() {
        SplitPane sp = new SplitPane();

        Tab params = new Tab("Params", createParamTable());
        Tab auth = new Tab("Authorization");
        Tab headers = new Tab("Headers", createParamTable());
        Tab body = new Tab("Body", new TextArea());
        Tab response = new Tab("Response", createResponseTab());

        TabPane options = new TabPane();
        TextArea ta = new TextArea();

        options.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        options.getTabs().addAll(params, auth, headers, body, response);
        sp.getItems().add(options);
        sp.getItems().add(ta);
        sp.setOrientation(Orientation.VERTICAL);

        return new Tab("untitled", sp);
    }

    @SuppressWarnings("unchecked")
    private ScrollPane createResponseTab() {
        ScrollPane sp = new ScrollPane();
        Pane p = new Pane();
        sp.setContent(p);
        try {
            Parent xml = FXMLLoader.load(getClass().getResource("responsetab.fxml"));
            p.getChildren().add(xml);
            TableView<Param> headers = (TableView<Param>) p.lookup("#headers");
            headers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
            headers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("value"));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return sp;
    }

    private TableView<Param> createParamTable() {
        TableView<Param> paramsTable = new TableView<>();

        TableColumn<Param, String> name = createColumn("Name", Param::name);
        TableColumn<Param, String> value = createColumn("Value", Param::value);

        name.prefWidthProperty().bind(paramsTable.widthProperty().multiply(0.5));
        value.prefWidthProperty().bind(paramsTable.widthProperty().multiply(0.5));

        name.setOnEditCommit(t -> {
            TableView<Param> tv = t.getTableView();
            t.getRowValue().name().set(t.getNewValue());
            manageEmptyCells(t, tv);
        });
        value.setOnEditCommit(t -> {
            TableView<Param> tv = t.getTableView();
            t.getRowValue().value().set(t.getNewValue());
            manageEmptyCells(t, tv);
        });

        paramsTable.getColumns().add(name);
        paramsTable.getColumns().add(value);

        paramsTable.setEditable(true);
        paramsTable.getItems().add(new Param());

        return paramsTable;
    }

    private void manageEmptyCells(TableColumn.CellEditEvent<Param, String> t, TableView<Param> tv) {
        if (t.getRowValue().isEmpty() && tv.getItems().size() > 1) {
            tv.getItems().remove(t.getRowValue());
        }
        if (!tv.getItems().get(tv.getItems().size() - 1).isEmpty()) {
            tv.getItems().add(new Param());
        }
        requestInput.setText(URLGenerator.processNewUrl(getParamsMap(), requestInput.getText()));
    }

    private TableColumn<Param, String> createColumn(String title, Function<Param, StringProperty> property) {
        TableColumn<Param, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));

        col.setCellFactory(column -> EditCell.createStringEditCell());
        return col ;
    }

    @FXML
    private void start() {
        runningIndicatorPane.setVisible(true);
        new Thread(() -> {
            final String method = httpMethod.getValue();
            Optional<TextArea> textArea = getCurrentTextArea();
            Optional<Response> response = Optional.empty();
            if (textArea.isPresent()) {
                try {
                    Request r = new RequestBuilder(requestInput.getText())
                            .setCustomHeaders(getCustomHeaders())
                            .build();
                    HttpClient hc = new HttpClient();
                    switch (method) {
                        case "GET" -> response = hc.get(r);
                        case "POST" -> response = hc.post(r, getBody());
                        case "PUT" -> response = hc.put(r, getBody());
                        case "DELETE" -> response = hc.delete(r);
                        case "HEAD" -> response = hc.head(r);
                    }
                    if (response.isPresent()) {
                        final Response res = response.get();
                        Platform.runLater(() -> {
                            textArea.get().setStyle(null);
                            textArea.get().setText(res.getBody());
                            updateResponseTab(res);
                            setSelectedTab(RequestTab.RESPONSE);
                        });
                    }
                } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    textArea.ifPresent(area -> Platform.runLater(() -> {
                        resetResponseTab();
                        area.setStyle("-fx-text-fill: red");
                        area.setText("Somethings went wrong: " + e.getMessage());
                    }));
                } finally {
                    Platform.runLater(() -> {
                        runningIndicatorPane.setVisible(false);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    runningIndicatorPane.setVisible(false);
                });
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void updateResponseTab(Response res) {
        Optional<Tab> responseTab = getCurrentResponseTab();
        if (responseTab.isPresent()) {
            Label status = (Label) responseTab.get().getContent().lookup("#status");
            status.setText(String.format("%s %s", res.getStatusCode(), res.getStatus()));
            if (res.getStatusCode() >= 100 && res.getStatusCode() < 200) {
                status.setTextFill(Color.web(StatusColor.INFORMATION.getHex()));
            } else if (res.getStatusCode() >= 200 && res.getStatusCode() < 300) {
                status.setTextFill(Color.web(StatusColor.SUCCESS.getHex()));
            } else if (res.getStatusCode() >= 300 && res.getStatusCode() < 400) {
                status.setTextFill(Color.web(StatusColor.REDIRECTION.getHex()));
            } else if (res.getStatusCode() >= 400 && res.getStatusCode() < 500) {
                status.setTextFill(Color.web(StatusColor.ERROR_CLIENT.getHex()));
            } else if (res.getStatusCode() >= 500) {
                status.setTextFill(Color.web(StatusColor.ERROR_SERVER.getHex()));
            }
            Label time = (Label) responseTab.get().getContent().lookup("#time");
            time.setText(String.format("%s ms", res.getTime()));
            TextArea raw = (TextArea) responseTab.get().getContent().lookup("#raw");
            raw.setText(res.getRawResponse());
            TextArea request = (TextArea) responseTab.get().getContent().lookup("#request");
            request.setText(res.getRequest().getRawRequest());
            TableView<Param> headers = (TableView<Param>) responseTab.get().getContent().lookup("#headers");
            headers.getItems().clear();
            for (Map.Entry<String, String> entry : res.getHeaders().entrySet()) {
                headers.getItems().add(new Param(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void setSelectedTab(RequestTab rt) {
        Optional<TabPane> options = getRequestOptionsTab();
        options.ifPresent(tabPane -> tabPane.getSelectionModel().select(rt.getIndex()));
    }

    @SuppressWarnings("unchecked")
    private void resetResponseTab() {
        Optional<Tab> responseTab = getCurrentResponseTab();
        if (responseTab.isPresent()) {
            Label status = (Label) responseTab.get().getContent().lookup("#status");
            status.setTextFill(Color.web(StatusColor.DEFAULT.getHex()));
            status.setText("...");
            Label time = (Label) responseTab.get().getContent().lookup("#time");
            time.setText("... ms");
            TextArea raw = (TextArea) responseTab.get().getContent().lookup("#raw");
            raw.setText("");
            TableView<Param> headers = (TableView<Param>) responseTab.get().getContent().lookup("#headers");
            headers.getItems().clear();
        }
    }

    private HashMap<String, String> getCustomHeaders() {
        HashMap<String, String> result = new HashMap<>();
        Optional<TabPane> tabs = getRequestOptionsTab();
        if (tabs.isPresent()) {
            Node n = tabs.get().getTabs().get(RequestTab.HEADERS.getIndex()).getContent();
            toHashMap(result, n, true);
        }
        return result;
    }

    private String getBody() {
        Optional<TabPane> tabs = getRequestOptionsTab();
        if (tabs.isPresent()) {
            Node n = tabs.get().getTabs().get(RequestTab.BODY.getIndex()).getContent();
            if (n instanceof TextArea) {
                return ((TextArea) n).getText();
            }
        }
        return "";
    }

    private HashMap<String, String> getParamsMap() {
        HashMap<String, String> result = new HashMap<>();
        Optional<TabPane> tabs = getRequestOptionsTab();
        if (tabs.isPresent()) {
            Node n = tabs.get().getTabs().get(RequestTab.PARAMS.getIndex()).getContent();
            toHashMap(result, n, false);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void toHashMap(HashMap<String, String> result, Node n, boolean lowering) {
        if (n instanceof TableView) {
            TableView<Param> tv = (TableView<Param>) n;
            for (Param p : tv.getItems()) {
                if (!p.isEmpty()) {
                    String key = (lowering) ? p.getName().toLowerCase() : p.getName();
                    result.put(key, p.getValue());
                }
            }
        }
    }

    private Optional<TabPane> getRequestOptionsTab() {
        Optional<SplitPane> requestTab = getCurrentRequestTab();
        if (requestTab.isPresent()) {
            Node n = requestTab.get().getItems().get(0);
            if (n instanceof TabPane) {
                return Optional.of((TabPane) n);
            }
        }
        return Optional.empty();
    }

    private Optional<SplitPane> getCurrentRequestTab() {
        Node n = tabs.getSelectionModel().getSelectedItem().getContent();
        if (n instanceof SplitPane) {
            return Optional.of((SplitPane) n);
        }
        return Optional.empty();
    }

    private Optional<TextArea> getCurrentTextArea() {
        Optional<SplitPane> requestTab = getCurrentRequestTab();
        if (requestTab.isPresent()) {
            Node n = requestTab.get().getItems().get(1);//
            if (n instanceof TextArea) {
                return Optional.of((TextArea) n);
            }
        }
        return Optional.empty();
    }

    private Optional<Tab> getCurrentResponseTab() {
        Optional<TabPane> requestTab = getRequestOptionsTab();
        if (requestTab.isPresent()) {
            Tab n = requestTab.get().getTabs().get(RequestTab.RESPONSE.getIndex());
            return Optional.of(n);
        }
        return Optional.empty();
    }

    @FXML
    private void createNewTab() {
        new Thread(() -> {
            Tab t = newTab();
            requests.put(t.hashCode(), "");
            Platform.runLater(() -> {
                tabs.getTabs().add(t);
                tabs.getSelectionModel().select(t);
            });
        }).start();
    }

    public void setStageAndSetupListeners(Stage s) {
        primaryStage = s;
    }

    @FXML
    private void showAboutDialog() {
        try {
            Stage dialog = new Stage();
            Parent xml = FXMLLoader.load(getClass().getResource("about.fxml"));
            dialog.initOwner(primaryStage);
            dialog.setScene(new Scene(xml, 677, 365));
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

    @FXML
    private void requestInputOnKeyPressed() {
        Tab tab = tabs.getSelectionModel().getSelectedItem();
        requests.put(tab.hashCode(), requestInput.getText());
    }

}
