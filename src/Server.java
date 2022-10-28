import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
//    ArrayList<Socket> socketList = new ArrayList<Socket>();
    HashMap<String, Socket> socketList = new HashMap<>();

    HashMap<String, String> account = new HashMap<>();
    private String id = "";

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public Server(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while(true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();

            synchronized (socketList) {
                // check if the user logged in the account, if yes then put it in to the socket list(hashmap)
                socketList.put(id, clientSocket);
            }

            Thread t = new Thread(()-> {
                try {
                    serve(clientSocket);
                } catch (IOException ex) {
                    print("Connection drop!");
                }

                synchronized (socketList) {
                    socketList.remove(id);
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
            StringBuilder msg = new StringBuilder("FORWARD: ");
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg.append(new String(buffer, 0, len));
                size -= len;
            }

            forward(msg.toString());

        }
    }

    private void forward(String msg){
        synchronized (socketList) {
//            for (Socket socket : socketList) {
//                try {
//                    System.out.println("Socket: "+socket);
//                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//                    out.writeInt(msg.length());
//                    out.write(msg.getBytes(), 0, msg.length());
//                } catch (IOException ex) {
//                    print("Unable to forward message to %s:%d\n",
//                            socket.getInetAddress().getHostName(), socket.getPort());
//                }
//            }
        }
    }

    private StringBuilder read_header(Socket clientSocket){
        byte[] buffer = new byte[1024];
        StringBuilder msg = new StringBuilder("");
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            int size = in.readInt();
            msg = new StringBuilder("");
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg.append(new String(buffer, 0, len));
                size -= len;
            }
        }catch (Exception e){
            System.out.println("Fail in reading msg");
        }
        return msg;
    }

    private StringBuilder read_txt(Socket clientSocket){
        byte[] buffer = new byte[1024];
        StringBuilder msg = new StringBuilder("");
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            int size = in.readInt();
            msg = new StringBuilder("");
            while(size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg.append(new String(buffer, 0, len));
                size -= len;
            }
        }catch (Exception e){
            System.out.println("Fail in reading msg");
        }
        return msg;
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new Server(port);
    }
}