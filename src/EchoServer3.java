import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class EchoServer3 {
    ArrayList<Socket> socketList = new ArrayList<Socket>();

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public EchoServer3(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while(true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();

            synchronized (socketList) {
                socketList.add(clientSocket);
            }

            Thread t = new Thread(()-> {
                try {
                    serve(clientSocket);
                } catch (IOException ex) {
                    print("Connection drop!");
                }

                synchronized (socketList) {
                    socketList.remove(clientSocket);
                }
            });
            t.start();
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
            String msg = "FORWARD: ";
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg += new String(buffer, 0, len);
                size -= len;
            }

            forward(msg);

        }
    }

    private void forward(String msg){
        synchronized (socketList) {
            for (Socket socket : socketList) {
                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeInt(msg.length());
                    out.write(msg.getBytes(), 0, msg.length());
                } catch (IOException ex) {
                    print("Unable to forward message to %s:%d\n",
                            socket.getInetAddress().getHostName(), socket.getPort());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new EchoServer3(port);
    }
}