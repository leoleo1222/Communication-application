import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static String username; // the username of the user
    private static String password; // the password of the user
    public boolean status; // the online/offline status of the user
    private static boolean login_success = false;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    byte[] buffer = new byte[1024];

    public Client(String serverIP, int port) throws IOException {
        System.out.printf("Connecting to %s:%d\n", serverIP, port);
        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[1024];
        Scanner sc = new Scanner(System.in);
        System.out.println("1)Registration\n2)Login");
        int option = sc.nextInt(); // choose register or login
        sc.nextLine();
        if (option == 1) { // Registration
            System.out.println("=== Registration ===");
            // input the username and password
            System.out.println("Input username:");
            username = sc.nextLine();
            System.out.println("Input password:");
            password = sc.nextLine();

            // TODO
            // pass the username and password to the server
            // in the server it will store these information with hashmap
            // the hashmap can check if the user entered the correct password

        } else if (option == 2) { // Login
            // TODO
            // check the server if the account exist
            // the server should have public hashmap which contain the username and password

            // if it exist then login success
            login_success = true;
            // else fail then tell the user it is fail in the login process
            
            while (true) {
                System.out.println("Input message and press ENTER");
                String message = sc.nextLine();

                int size = message.length();
                out.writeInt(size);
                out.write(message.getBytes(), 0, size);

                message = "";
                size = in.readInt();
                while (size > 0) {
                    int len = in.read(buffer, 0, Math.min(size, buffer.length));
                    message += new String(buffer, 0, len);
                    size -= len;
                }
                System.out.println(message);
            }
        }

    }

    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int port = 12345;
        try {
            new Client(serverIP, port);
        } catch (Exception e) {
            System.out.println("Init Client fail");
        }


    }


}
