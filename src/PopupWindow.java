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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PopupWindow extends Application{
    // for login
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    /// ComboBox requires the data type of its child items
    @FXML private Button LogInAndRegister;
    @FXML private ComboBox<String> cbxFlag;
    @FXML private CheckBox chkAgreement;
    @FXML private RadioButton rdoDay, rdoNight;
    @FXML private Button btnLogin, btnCancel;

    Stage stage;

    public String username, password, flag;
    public boolean loggedIn = false;

    public DataOutputStream out;
    public DataInputStream in;

    public PopupWindow() throws IOException {
        stage = new Stage();
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("PopupWindow.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root, 300, 280);
        stage.setScene(scene);
        stage.setTitle("Pop Up");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PopupWindow.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Welcome");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    @FXML
    protected void initialize() throws IOException{
        Socket socket = new Socket("127.0.0.1", 12345);

        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        LogInAndRegister.setOnMouseClicked(event -> {
        username = txtUsername.getText();
        password = txtPassword.getText();
            do {
                sendString("reg", out);
                sendString(username, out);
                sendString(password, out);
            } while (!receiveString(in).equals(password));
            loggedIn = true;
            stage.close();
        });
    }

    public String receiveString(DataInputStream in) {
        String res = "";
        try {
            byte[] buffer = new byte[1024];
            int len = in.readInt();
            while (len > 0) {
                int l = in.read(buffer, 0, Math.min(len, buffer.length));
                res += new String(buffer, 0, l);
                len -= l;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public void sendString(String str, DataOutputStream out) {
        try {
            // send the header to the server
            int size = str.length();
            out.writeInt(size);
            out.write(str.getBytes(), 0, size);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
