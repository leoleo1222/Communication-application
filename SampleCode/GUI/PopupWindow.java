/*
1. Put the fxml file together with the java files.
2. Delete the attribute about the controller from the root control.
3. Delete all xmlns attributes from the root control.
4. Add xmlns:fx="http://javafx.com/fxml" as an attribute of the root control.
5. Save the changes of the fxml file.
*/

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class PopupWindow {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    /// ComboBox requires the data type of its child items
    @FXML private ComboBox<String> cbxFlag;
    @FXML private CheckBox chkAgreement;
    @FXML private RadioButton rdoDay, rdoNight;
    @FXML private Button btnLogin, btnCancel;

    Stage stage;

    public String username, password, flag;
    public boolean agree;
    public int mode;
    public boolean isCancelled;

    public PopupWindow() throws IOException {
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PopupWindow.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        Scene scene = new Scene(root, 300, 280);
        stage.setScene(scene);
        stage.setTitle("Pop Up");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }

    @FXML
    protected void initialize() {
        String[] items = {"FLAG-A", "FLAG-C", "FLAG-Z"};
        cbxFlag.setItems(FXCollections.observableArrayList(items));

        btnLogin.setOnMouseClicked(event->{
            username = txtUsername.getText();
            password = txtPassword.getText();
            flag = (String) cbxFlag.getValue();
            agree = chkAgreement.isSelected();
            mode = rdoDay.isSelected()? 0: 1;
            isCancelled = false;
            stage.close();
        });

        btnCancel.setOnMouseClicked(event->{
            isCancelled = true;
            stage.close();
        });
    }
}
