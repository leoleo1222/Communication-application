import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {
    public static void print(String str) {
        System.out.print(str);
    }

    public static void println(char c) {
        System.out.println(c);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        print("Server IP: ");
        String ip = scanner.nextLine();
        print("TCP Port: ");
        int port = scanner.nextInt();
        Socket socket = new Socket(ip, port);
        InputStream in = socket.getInputStream();

        while(true) {
            char c = (char) in.read();
            println(c);
        }
    }
}
