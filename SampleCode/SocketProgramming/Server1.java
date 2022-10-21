import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server1 {
    public static void printf(String str, Object... o) {
        System.out.printf(str, o);
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public Server1(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            printf("Listening to port %d...\n", port);
            Socket socket = serverSocket.accept();
            Thread t = new Thread(() -> {
                try {
                    serve(socket);
                } catch (IOException ex) {
                    println("Connection dropped!");
                }
            });
            t.start();
        }
    }

    private void serve(Socket clientSocket) throws IOException {
        byte[][] greetings = {
            "Good to see you!".getBytes(),
            "How are you today?".getBytes(),
            "How do you do?".getBytes(),
            "Have a nice day.".getBytes()
        };
        printf("Connected to %s:%d\n",
                clientSocket.getInetAddress().getHostAddress(),
                clientSocket.getPort());
        OutputStream out = clientSocket.getOutputStream();
        Random random = new Random(1);
        while (true) {
            int index = Math.abs(random.nextInt()) % greetings.length;
            byte[] msg = greetings[index];
            out.write(msg, 0, msg.length);
        }
    }

    public static void main(String[] args) throws IOException {
        new Server1(12345);
    }
}
