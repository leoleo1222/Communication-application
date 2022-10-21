import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

public class ChatUI1 extends Application {

    ObservableList<Node> children;
    int msgIndex = 0;

    ScrollPane scrollPane;
    TextField txtInput;

    ChatUI2 chat;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat");

        VBox messagePane = new VBox();
        children = messagePane.getChildren();

        scrollPane = new ScrollPane();
        scrollPane.setContent(messagePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        messagePane.heightProperty().addListener(event->{
            scrollPane.setVvalue(1);
        });

        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(500);

        txtInput = new TextField();
        txtInput.setOnKeyPressed(event->{
            if (event.getCode().toString().equals("ENTER"))
                displayMessage();
        });

        Button btnEmoji = new Button(":)");
        btnEmoji.setOnMouseClicked(event->{
            displayEmoji();
        });

        BorderPane container = new BorderPane();
        container.setCenter(txtInput);
        container.setRight(btnEmoji);

        root.setBottom(container);

        primaryStage.show();
    }

    private Node messageNode(String text, boolean alignToRight) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(10, 10, 10, 10));

        if (alignToRight)
            box.setAlignment(Pos.BASELINE_RIGHT);
        Label label = new Label(text);
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
            scrollPane.setVvalue(1);
        });
    }

    private void displayEmoji() {
        Platform.runLater(() -> {
            children.add(imageNode("emoji.png", msgIndex == 0));
            msgIndex = (msgIndex + 1) % 2;
            scrollPane.setVvalue(1);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}