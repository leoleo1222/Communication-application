import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    public static String username; // the username of the user
    private static String password; // the password of the user
    private static boolean registered = false; // check if the user registered

    public Client2(String serverIP, int port) throws IOException {
        System.out.printf("Connecting to %s:%d\n", serverIP, port);
        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[1024];
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("1)Registration\n2)Login");
            int option = sc.nextInt(); // choose register or login
            sc.nextLine();
            if (option == 1) { // Registration
                System.out.println("=== Registration ===");
                // input the username
                System.out.println("Input username:");
                username = sc.nextLine();
                // input the password
                System.out.println("Input password:");
                password = sc.nextLine();
                // send the header as reg, it means the sending msg is a registration msg
                String header = "reg";
                sendString(header, out);
                sendString(username, out);
                sendString(password, out);
            }
            if (option == 2) { // Login
                // TODO
                // check the server if the account exist
                // the server should have public hashmap which contain the username and password
                while (true) { // sending msg
                    String header = "single";
                    sendString(header, out);
                    System.out.println("Enter a receiver name:");
                    String receiver = sc.nextLine();
                    sendString(receiver, out);
                    System.out.println("Input message and press ENTER");
                    String message = sc.nextLine();
                    sendString(message, out);
                }
            }
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

                        System.out.println(receive);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }

    public void sendString(String str, DataOutputStream out) {
        try {
            // send the header to the server
            int size = str.length();
            out.writeInt(size);
            out.write(str.getBytes(), 0, size);
        } catch (Exception e) {
            e.printStackTrace();
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
