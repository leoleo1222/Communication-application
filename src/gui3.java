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

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class gui3 extends Application {
    //private static Client client;
    ObservableList<Node> children;
    ObservableList<Node> listChildren;

    String[] header = {"reg", "single", "group", "showList", "exit"};
    private String[][] dmList = new String[0][0];
    private String[][] groupList = new String[0][0];
    public String receiver;

    @FXML
    private ScrollPane scrollPaneL;
    @FXML
    private ScrollPane scrollPaneC;
    @FXML
    private TextField txtInput;
    @FXML
    private TextField nameText;
    @FXML
    private Button upload;
    @FXML
    private Button swapMode;
    @FXML
    private Button add;
    @FXML
    private Button createGroup;
    @FXML
    private VBox messagePane;
    @FXML
    private VBox listPane;
    @FXML
    private Label receiverName;

    public String username, password;
    public static DataOutputStream out;
    public static DataInputStream in;
    public byte[] buffer = new byte[1024];

    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat");
        primaryStage.setResizable(false);
        primaryStage.show();
        createGroup.setVisible(false);

        Thread t = new Thread(() -> {
            try {
                while (true) { // receiving msg
                    String receive = "";
                    int r_size = in.readInt();
                    while (r_size > 0) {
                        int len = in.read(buffer, 0, Math.min(r_size, buffer.length));
                        receive += new String(buffer, 0, len);
                        r_size -= len;
                    }

                    if(swapMode.getText().equals("Individual")){
                        if(receive.startsWith("System")){ listIndividual(receive);
                        }else receive(receive.replaceAll("Single->", ""));
                    }else{
                        if(receive.startsWith("System")){ listGroup(receive);
                        }else receive(receive.replaceAll("Group->", ""));
                    }

                    System.out.println(receive);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

    public void receive(String receive) {
        Platform.runLater(() -> {
            children.add(messageNode(receive, false));
        });
    }


    @FXML
    protected void initialize() {
        children = messagePane.getChildren();
        listChildren = listPane.getChildren();

        messagePane.heightProperty().addListener(event -> {
            scrollPaneC.setVvalue(1);
        });

        listPane.heightProperty().addListener(event -> {
            scrollPaneL.setVvalue(1);
        });

        txtInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER"))
                sendMessage();
        });

        swapMode.setOnMouseClicked(event -> {
            if(swapMode.getText().equals("Individual")) createGroup.setVisible(true);
            else createGroup.setVisible(false);
            swapMode();
        });

        createGroup.setOnMouseClicked(event -> {
            try {
                createGroup pop = new createGroup();
                if (pop.loggedIn) {
                    pop.in = in;
                    pop.out = out;
                }
                else
                    System.out.print("Cancelled\n");

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        add.setOnMouseClicked(event -> {
            if(swapMode.getText().equals("Individual")) {
                sendString(header[3], out);
            }else{
                sendString(header[2],out);
                sendString("show",out);
            }
        });

        upload.setOnMouseClicked(event -> {
            sendMessage();
        });

        try {
            PopupWindow pop = new PopupWindow();
            if (pop.loggedIn) {
                print("username: %s\npassword: %s\nflag:%s\n",
                        pop.username,
                        pop.password,
                        pop.flag);

                username = pop.username;
                password = pop.password;
                out = pop.out;
                in = pop.in;
            }
            else
                System.out.print("Cancelled\n");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        sendString("reg", out);
        sendString(username, out);
        sendString(password, out);
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

    private void sendMessage() {
        Platform.runLater(() -> {
            String text = txtInput.getText();
            txtInput.clear();
            children.add(messageNode(text, true));
            sendString(header[1] , out);
            sendString(receiver, out);
            sendString(text, out);

            System.out.println(receiver + "," + text);
        });
    }

    private void swapMode() {
        Platform.runLater(() -> {
            // Clear list
            listPane.getChildren().remove(1, listPane.getChildren().size());
            listChildren = listPane.getChildren();

            // Add list
            if (swapMode.getText().equals("Group")) {
                swapMode.setText("Individual");
                //for (int i = 0; ;i++);  //listChildren.add(ind[i]);

            }
            else {
                swapMode.setText("Group");
                //for (int i = 0; ;i++);  //listChildren.add(grp[i]);

            }
        });
    }

    private void uploadFile() {
        Platform.runLater(() -> {

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

    public void listIndividual(String receive) {
        Platform.runLater(() -> {
            String[] list = receive.split(",");
            listPane.getChildren().remove(1, listPane.getChildren().size());
            for (int i = 1; i < list.length; i++) addBox(list[i]);
        });
    }

    public void listGroup(String receive) {
        Platform.runLater(() -> {
            String[] list = receive.split(",");
            listPane.getChildren().remove(1, listPane.getChildren().size());
            for (int i = 1; i < list.length; i++) addBox(list[i]);
        });
    }

    private void addBox(String name) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 10, 0, 5));
        javafx.scene.control.Label label = new Label(name);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
            addToList(name);
        });
        listPane.getChildren().add(box);
    }

    private void addToList(String individual) {
        String[][] newDmList = new String[dmList.length+1][1];
        for (int i = 0; i < dmList.length; i++) {
            newDmList[i] = new String[dmList[i].length];
            if (dmList[i][0].equals(individual)) {
                System.out.println("duplicated person");
                restoreList();
                return;
            }
            for (int j = 0; j < dmList[i].length; j++) {
                newDmList[i][j] = dmList[i][j];
            }
        }
        newDmList[dmList.length][0] = individual;
        dmList = newDmList;
        restoreList();
    }

    private void restoreList() {
        listPane.getChildren().remove(1, listPane.getChildren().size());
        for (int i = 0; i < dmList.length; i++) dmBox(dmList[i]);
    }

    private void dmBox(String[] individual) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 10, 0, 5));
        javafx.scene.control.Label label = new Label(individual[0]);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
            changeDm(individual);
            receiver = individual[0];
            receiverName.setText(receiver);
            System.out.println(receiver);
        });
        listPane.getChildren().add(box);
    }

    private void changeDm(String[] dms) {
        children.clear();
        for(int i = 1; i < dms.length; i++) children.add(messageNode(dms[i], false));
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}