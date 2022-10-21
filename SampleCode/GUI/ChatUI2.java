/*
1. Put the fxml file together with the java files.
2. Delete the attribute about the controller from the root control.
3. Delete all xmlns attributes from the root control.
4. Add xmlns:fx="http://javafx.com/fxml" as an attribute of the root control.
5. Save the changes of the fxml file.
*/


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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

public class ChatUI2 extends Application {
    ObservableList<Node> children;
    int msgIndex = 0;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField txtInput;
    @FXML
    private Button btnEmoji;
    @FXML
    private VBox messagePane;


    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatUI2.fxml"));
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
        children = messagePane.getChildren();

        messagePane.heightProperty().addListener(event -> {
            scrollPane.setVvalue(1);
        });

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER"))
                displayMessage();
        });

        btnEmoji.setOnMouseClicked(event -> {
            displayEmoji();
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

    private Node imageNode(String imagePath, boolean alignToRight) {
        try {
            HBox box = new HBox();
            box.paddingProperty().setValue(new Insets(10, 10, 10, 10));

            if (alignToRight)
                box.setAlignment(Pos.BASELINE_RIGHT);
            FileInputStream in = new FileInputStream(imagePath);
            ImageView imageView = new ImageView(new Image(in));
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            box.getChildren().add(imageView);
            return box;
        } catch (IOException ex) {
            ex.printStackTrace();
            return messageNode("!!! Fail to display an image !!!", alignToRight);
        }
    }

    private void displayMessage() {
        Platform.runLater(() -> {
            String text = txtInput.getText();
            txtInput.clear();
            children.add(messageNode(text, msgIndex == 0));
            msgIndex = (msgIndex + 1) % 2;
        });
    }

    private void displayEmoji() {
        Platform.runLater(() -> {
            children.add(imageNode("emoji.png", msgIndex == 0));
            msgIndex = (msgIndex + 1) % 2;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
