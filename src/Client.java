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

    public Client(String serverIP, int port) throws IOException {
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
                System.out.println("Input username:");
                username = sc.nextLine();
                System.out.println("Input password:");
                password = sc.nextLine();
                String header = "reg";
                int header_size = header.length();
                out.writeInt(header_size);
                out.write(header.getBytes(), 0, header_size);
                int username_size = username.length();
                out.writeInt(username_size);
                out.write(username.getBytes(), 0, username_size);
                int password_size = password.length();
                out.writeInt(password_size);
                out.write(password.getBytes(), 0, password_size);
            } else if (option == 2) { // Login
                // TODO
                // check the server if the account exist
                // the server should have public hashmap which contain the username and password
                while (true) {
                    System.out.println("Enter a receiver name:");
                    String header = sc.nextLine();
                    int header_size = header.length();
                    out.writeInt(header_size);
                    out.write(header.getBytes(), 0, header_size);
                    System.out.println("Input message and press ENTER");
                    String message = sc.nextLine();
                    int size = message.length();
                    out.writeInt(size);
                    out.write(message.getBytes(), 0, size);
                }
            }
            Thread t = new Thread(() -> {
                try {
                    while (true) {
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
                    System.out.println("Error msg in receiving: "+e.getMessage());
                }
            });
            t.start();
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
