import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
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
        InputStream in = socket.getInputStream();

        while (true) {
            int len = in.read(buffer, 0, buffer.length);
            println(new String(buffer, 0, len));
        }
    }
}
