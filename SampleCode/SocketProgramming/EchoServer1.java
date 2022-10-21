import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer1 {

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public EchoServer1(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while(true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();
            serve(clientSocket);
        }
    }
    private void serve(Socket clientSocket) throws IOException {
        byte[] buffer = new byte[1024];
        print("Established a connection to host %s:%d\n\n",
                clientSocket.getInetAddress(), clientSocket.getPort());

        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        while(true) {
            int size = in.readInt();
            String msg = "ECHO: ";
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg += new String(buffer, 0, len);
                size -= len;
            }

            out.writeInt(msg.length());
            out.write(msg.getBytes(), 0, msg.length());
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        new EchoServer1(port);
    }
}