import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public EchoClient(String serverIP, int port) throws IOException {
        print("Connecting to %s:%d\n", serverIP, port);

        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);
        byte[] buffer = new byte[1024];

        while(true) {
            print("Input message and press ENTER\n");
            String message = scanner.nextLine();

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
            print(message + "\n");
        }
    }

    public static void main(String[] args) throws IOException {
        String serverIP = args[0];
        int port = Integer.parseInt(args[1]);
        new EchoClient(serverIP, port);
    }
}
