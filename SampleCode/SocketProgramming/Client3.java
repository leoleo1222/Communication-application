import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {
    public static void print(String str) {
        System.out.print(str);
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        print("Server IP: ");
        String ip = scanner.nextLine();

        print("TCP Port: ");
        int port = scanner.nextInt();

        byte[] buffer = new byte[1024];
        Socket socket = new Socket(ip, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());

        while (true) {
            int remain = in.readInt();
            print("\n## ");
            while(remain > 0) {
                int len = in.read(buffer, 0, Math.min(buffer.length, remain));
                print(new String(buffer, 0, len));
                remain -= len;
            }
        }
    }
}
