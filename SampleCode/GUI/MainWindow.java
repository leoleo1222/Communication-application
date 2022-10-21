/*
1. Put the fxml file together with the java files.
2. Delete the attribute about the controller from the root control.
3. Delete all xmlns attributes from the root control.
4. Add xmlns:fx="http://javafx.com/fxml" as an attribute of the root control.
5. Save the changes of the fxml file.
*/

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow extends Application {
    @FXML private Button btnPopup;

    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        Scene scene = new Scene(root, 150, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Main");
        primaryStage.setResizable(false);
        primaryStage.show();

        /// terminate the program when the user clicks the close button
        primaryStage.setOnCloseRequest(event ->{
            System.exit(0);
        });
    }

    @FXML
    public void initialize() {
        btnPopup.setOnMouseClicked(event->{
            try {
                PopupWindow pop = new PopupWindow();
                if (!pop.isCancelled)
                    print("username: %s\npassword: %s\nflag:%s\nagree: %s\nmode: %s\n",
                            pop.username,
                            pop.password,
                            pop.flag,
                            pop.agree,
                            pop.mode);
                else
                    System.out.print("Cancelled\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
