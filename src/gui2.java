import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class gui2 extends Application {
    //private static Client client;
    ObservableList<Node> children;
    ObservableList<Node> listChildren;

    String[] header = { "reg", "single", "group", "showList", "upload", "download", "exit" };
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

    public String username, password, filename;
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

                    if(receive.startsWith("System: ")) {
                        if(swapMode.getText().equals("Individual")) listIndividual(receive);
                        else if(swapMode.getText().equals("Group")) listGroup(receive);
                    }
                    else {
                        receive(receive);
                    }

                    System.out.println(receive);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

    public void receive(String receive){
        Platform.runLater(() -> {
            if(receive.contains("download")) {
                try {
                    download(receive);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(receive.startsWith("Single")) {
                receiveDm(receive.substring(8));
            }
            else if(receive.startsWith("Group")) {
                receiveGroup(receive.substring(7));
            }
        });
    }

    private void download(String receive) throws IOException {
        System.out.println("download");
        if(new File(username + "_download").mkdir()){
            int remain = in.readInt(); // the size of the file
            String filename = ""; // the name of the file
            while (remain > 0) { // receive the file name
                int len = in.read(buffer, 0, Math.min(remain, buffer.length)); // read the file name
                filename += new String(buffer, 0, len); // append the file name
                remain -= len; // update the remain size
            }
            // create a file with the name inside the sender folder
            File file = new File(username + "_download" + "/" + filename);
            FileOutputStream fout = new FileOutputStream(file); // create a file output stream
            long size = in.readLong(); // read the file size
            System.out.printf("Downloading %s (%d bytes) ...\n", filename, size); // print the file name and size
            while (size > 0) { // receive the file
                int len = in.read(buffer, 0, (int) Math.min(size, buffer.length)); // read the file
                fout.write(buffer, 0, len); // write the file
                size -= len; // update the remain size
                System.out.printf("."); // print a dot to show the progress
            }
            System.out.printf("Completed!\n"); // print the complete msg
            fout.flush(); // flush the file output stream
            fout.close(); // close the file output stream
        }
    }

    private void receiveDm(String receive) {
        String[] message = receive.split(":", 2);
        String sender = message[0];
        if (sender.equals(receiver)) children.add(messageNode(message[1], false));
        for (int i = 0; i < dmList.length; i++) {
            if (dmList[i][0].equals(sender)) {
                addDm(i, message[1]);
                return;
            }
        }
        addToList(sender);
        addDm(dmList.length-1, message[1]);
    }

    private void addDm(int person, String message) {
        String[] messages = new String[dmList[person].length + 1];
        for (int i = 0; i < dmList[person].length; i++) {
            messages[i] = dmList[person][i];
        }
        messages[dmList[person].length] = message;
        dmList[person] = messages;
    }

    private void receiveGroup(String receive) {
        String[] message = receive.split(":", 2);
        String sender = message[0];
        if (sender.equals(receiver)) children.add(messageNode(message[1], false));
        for (int i = 0; i < groupList.length; i++) {
            if (groupList[i][0].equals(sender)) {
                addGroupDm(i, message[1]);
                return;
            }
        }
        addToGroupList(sender);
        addGroupDm(groupList.length-1,message[1]);
    }

    private void addGroupDm(int group, String message) {
        String[] messages = new String[groupList[group].length + 1];
        for (int i = 0; i < groupList[group].length; i++) {
            messages[i] = groupList[group][i];
        }
        messages[groupList[group].length] = message;
        groupList[group] = messages;
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
            if (event.getCode().toString().equals("ENTER")) {
                if (swapMode.getText().equals("Individual")) sendMessage();
                else if (swapMode.getText().equals("Group")) sendGroupMessage();
            }
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
            if(swapMode.getText().equals("Individual")) {
                filename = txtInput.getText();
                File file = new File(filename);
                if(file.exists()) {
                    sendString("upload",out);
                    sendString(username,out);
                    uploadFile(filename);
                    txtInput.clear();
                }else{
                    txtInput.setText("File Not Found!");
                    sendMessage();
                }
            }
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
            if (text.contains("!file:")) {
                sendString(header[4] , out);
                sendString(receiver, out);
                sendString(username, out);
                String path = text.substring(6);
                uploadFile(path);

                sendString(header[1] , out);
                sendString(receiver, out);
                sendString(username+":"+ text, out);
            }
            else {
                children.add(messageNode(text, true));
                sendString(header[1] , out);
                sendString(receiver, out);
                sendString(username+":"+ text, out);
            }


        });
    }

    private void sendGroupMessage() {
        Platform.runLater(() -> {
            String text = txtInput.getText();
            txtInput.clear();
            children.add(messageNode(text, true));
            sendString(header[2] , out);
            sendString("send", out);
            sendString(receiver, out);
            if (text.contains("!file:")) sendString(text, out);
            else sendString(username+":"+ text, out);
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
                restoreList();
            }
            else {
                swapMode.setText("Group");
                restoreGroupList();
            }
        });
    }

    private void uploadFile(String path) {
        Platform.runLater(() -> {
            try {
                File file = new File(path); // create a file object
                FileInputStream fin = new FileInputStream(file); // create a file input stream
                byte[] fileName = file.getName().getBytes(); // get the file name
                out.writeInt(fileName.length); // send the file name length to the server
                out.write(fileName, 0, fileName.length); // send the file name to the server
                long size = file.length(); // get the file size
                out.writeLong(size); // send the file size to the server
                System.out.printf("Uploading %s (%d bytes)", path, size); // print out the file name and size
                while (size > 0) {
                    int len = fin.read(buffer, 0, (int) Math.min(size, buffer.length)); // read the file
                    out.write(buffer, 0, len); // send the file to the server
                    size -= len; // update the file size
                    System.out.printf("."); // print out a dot
                }
                System.out.println("Complete!"); // print out complete
                out.flush(); // flush the output stream
                fin.close();
            } catch (IOException e){
                e.printStackTrace();
            }
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
            for (int i = 1; i < list.length; i++) addGroupBox(list[i]);
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

    private void addGroupBox(String name) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 10, 0, 5));
        javafx.scene.control.Label label = new Label(name);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
            addToGroupList(name);
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
        if (swapMode.getText().equals("Individual")) restoreList();
    }

    private void addToGroupList(String individual) {
        String[][] newDmList = new String[groupList.length+1][1];
        for (int i = 0; i < groupList.length; i++) {
            newDmList[i] = new String[groupList[i].length];
            if (groupList[i][0].equals(individual)) {
                System.out.println("duplicated group");
                restoreGroupList();
                return;
            }
            for (int j = 0; j < groupList[i].length; j++) {
                newDmList[i][j] = groupList[i][j];
            }
        }
        newDmList[groupList.length][0] = individual;
        groupList = newDmList;
        if (swapMode.getText().equals("Group")) restoreGroupList();
    }

    private void restoreList() {
        listPane.getChildren().remove(1, listPane.getChildren().size());
        for (int i = 0; i < dmList.length; i++) dmBox(i);
    }

    private void restoreGroupList() {
        listPane.getChildren().remove(1, listPane.getChildren().size());
        for (int i = 0; i < groupList.length; i++) groupBox(i);
    }

    private void dmBox(int individual) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 10, 0, 5));
        javafx.scene.control.Label label = new Label(dmList[individual][0]);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
            changeDm(individual);
            receiver = dmList[individual][0];
            receiverName.setText(receiver);
        });
        listPane.getChildren().add(box);
    }

    private void groupBox(int group) {
        HBox box = new HBox();
        box.paddingProperty().setValue(new Insets(5, 10, 0, 5));
        javafx.scene.control.Label label = new Label(groupList[group][0]);
        label.setWrapText(true);
        box.getChildren().add(label);
        box.setOnMouseClicked(event -> {
            changeGroup(group);
            receiver = groupList[group][0];
            receiverName.setText(receiver);
        });
        listPane.getChildren().add(box);
    }

    private void changeDm(int dms) {
        children.clear();
        System.out.println(dmList[dms][0]);
        System.out.println(dmList[dms].length);
        for(int i = 1; i < dmList[dms].length; i++) {
            children.add(messageNode(dmList[dms][i], false));
        }
    }

    private void changeGroup(int dms) {
        children.clear();
        System.out.println(groupList[dms][0]);
        System.out.println(groupList[dms].length);
        for(int i = 1; i < groupList[dms].length; i++) children.add(messageNode(groupList[dms][i], false));
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}