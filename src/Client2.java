import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    public static String username = " "; // the username of the user
    private static String password = " "; // the password of the user

    public Client2(String serverIP, int port) throws IOException {
        System.out.printf("Connecting to %s:%d\n", serverIP, port);
        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[1024];
        String[] header = {"reg", "single", "group"};
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("Registration/Login");
            // input the username
            System.out.println("Input username:");
            username = sc.nextLine();
            // input the password
            System.out.println("Input password:");
            password = sc.nextLine();
            // send the header as reg, it means the sending msg is a registration msg
            sendString(header[0], out);
            sendString(username, out);
            sendString(password, out);
        } while (!receiveString(in).equals(password));
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
        while (true) { // sending msg
            System.out.print("Type 1 to send a direct message, 2 to send a group message, ");
            System.out.println("Type @@quit to quit the session");
            int choice = sc.nextInt();
            sc.nextLine();
            if (choice == 1) {
                while (true){
                    sendString(header[1] , out);
                    System.out.println("Enter a receiver name:");
                    String receiver = sc.nextLine();
                    if (receiver.equals("@@quit")) break;
                    sendString(receiver, out);
                    System.out.println("Input message and press ENTER");
                    String message = sc.nextLine();
                    if (message.equals("@@quit")) break;
                    sendString(message, out);
                }
            } else if (choice == 2) {
                System.out.println("In group message mode");
//                groupMsg(out, sc);
            }
        }
    }

    private void groupMsg(DataOutputStream out, Scanner sc) {

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

    public static void main(String[] args) {
        String serverIP = "127.0.0.1";
        int port = 12345;
        try {
            new Client(serverIP, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
