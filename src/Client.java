import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    final String ID; // the ID of the user
    public String username; // the username of the user
    private String password; // the password of the user
    public boolean status; // the online/offline status of the user
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Scanner scanner = new Scanner(System.in);
    byte[] buffer = new byte[1024];



    Client(String ID, String username, String password) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.status = false;

        // setting the socket
        try {
            socket = new Socket("127.0.0.1", 12345);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("The server is off");
        }

    }

    private void change_password(String new_password) {
        this.password = new_password;
    }

    public String get_id() {
        return this.ID;
    }

    public String get_username() {
        return this.username;
    }

    public String get_password() {
        return this.password;
    }

    private void change_status(boolean s) {
        this.status = s;
    }

    public void send_msg() {
        System.out.println("Input message and press ENTER");
        String message = scanner.nextLine();
        try{
            int size = message.length();
            out.writeInt(size);
            out.write(message.getBytes(), 0, size);

            message = "";
            size = in.readInt();
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                message += new String(buffer, 0, len);
                size -= len;
            }
        }catch (Exception e){
            System.out.println("error during sending msg");
        }

    }


}
