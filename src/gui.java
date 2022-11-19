import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class gui extends Application {
    //private static Client client;
    ObservableList<Node> children;
    ObservableList<Node> listChildren;
    int msgIndex = 0;

    @FXML
    private ScrollPane scrollPaneL;
    @FXML
    private ScrollPane scrollPaneC;
    @FXML
    private TextField txtInput;
    @FXML
    private Button upload;
    @FXML
    private Button swapMode;
    @FXML
    private VBox messagePane;
    @FXML
    private VBox listPane;

    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Chat");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    @FXML
    protected void initialize() {
        new PopupWindow();
        children = messagePane.getChildren();

        messagePane.heightProperty().addListener(event -> {
            scrollPaneC.setVvalue(1);
        });

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER"))
                sendMessage();
        });

        swapMode.setOnMouseClicked(event -> {
            swapMode();
        });

        upload.setOnMouseClicked(event -> {
            uploadFile();
        });
    }

    private Node messageNode(String text, boolean alignToRight) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(10, 10, 10, 10));

        if (alignToRight)
            box.setAlignment(Pos.BASELINE_RIGHT);
        javafx.scene.control.Label label = new Label(text);
        label.setWrapText(true);
        box.getChildren().add(label);
        return box;
    }

    private void sendMessage() {
        Platform.runLater(() -> {
            String text = txtInput.getText();
            txtInput.clear();
            children.add(messageNode(text, true));
            //msgIndex = (msgIndex + 1) % 2;
            //client.sendString(text, client.out);
        });
    }

    private void swapMode() {
        Platform.runLater(() -> {
            // Clear list
            listPane = new VBox();
            listChildren = listPane.getChildren();

            // Add list
            if (swapMode.getText().equals("Group")) {
                swapMode.setText("Individual");
                for (int i = 0; ;i++);  //listChildren.add(ind[i]);

            }
            else {
                swapMode.setText("Group");
                for (int i = 0; ;i++);  //listChildren.add(grp[i]);

            }
        });
    }

    private void uploadFile() {
        Platform.runLater(() -> {
            
        });
    }

    public static void main(String[] args) throws IOException {
        String serverIP = "127.0.0.1";
        int port = 12345;


        Thread t1 = new Thread(()-> {
            launch(args);
        });
        t1.start();

        /*try {
            client = new Client(serverIP, port);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
