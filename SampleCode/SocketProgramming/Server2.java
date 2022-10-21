import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server2 {
    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public Server2(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            print("Listening to port %d...\n", port);
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
        print("Connected to %s:%d\n",
                clientSocket.getInetAddress().getHostAddress(),
                clientSocket.getPort());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        Random random = new Random(1);
        while (true) {
            int index = Math.abs(random.nextInt()) % greetings.length;
            byte[] msg = greetings[index];
            out.writeInt(msg.length);
            out.write(msg, 0, msg.length);
        }
    }

    public static void main(String[] args) throws IOException {
        new Server2(12345);
    }
}
