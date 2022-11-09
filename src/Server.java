import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    //    ArrayList<Socket> socketList = new ArrayList<Socket>();
    HashMap<String, Socket> socketList = new HashMap<>();

    HashMap<String, String> account = new HashMap<>();
    private static String id = "";

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public Server(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while (true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();

            // reading the header
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            byte[] buffer = new byte[1024];
            String header = "";
            int size = in.readInt();
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                header += new String(buffer, 0, len);
                size -= len;
            }

            // registration process
            if (header.startsWith("reg")) {
                String username = "";
                size = in.readInt();
                while (size > 0) {
                    int len = in.read(buffer, 0, Math.min(size, buffer.length));
                    username += new String(buffer, 0, len);
                    size -= len;
                }
                String password = "";
                size = in.readInt();
                while (size > 0) {
                    int len = in.read(buffer, 0, Math.min(size, buffer.length));
                    password += new String(buffer, 0, len);
                    size -= len;
                }
                account.put(username, password);
                id = username;
                System.out.println(id + " logged in");
            }

            synchronized (socketList) {
                socketList.put(id, clientSocket);
            }

            Thread t = new Thread(() -> {
                try {
                    serve(clientSocket);
                } catch (IOException ex) {
                    print("Connection drop!");
                }

                synchronized (socketList) {
                    System.out.println(id + " is offline");
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


        while (true) {
            int i = 1;
            String namelist = "";
            for (String username : socketList.keySet()) {
                namelist += "[" + i++ + "]" + username + "\n";
            }
            out.writeInt(namelist.length());
            out.write(namelist.getBytes(), 0, namelist.length());


            String target = "";
            int size = in.readInt();
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                target += new String(buffer, 0, len);
                size -= len;
            }


            size = in.readInt();
            StringBuilder msg = new StringBuilder("");
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg.append(new String(buffer, 0, len));
                if(!socketList.containsKey(target) && account.containsKey(target)){
                    File file = new File(target + ".txt");
                    FileOutputStream out_file = new FileOutputStream(file);
                    System.out.println("Saved into " + target);
                    out_file.write(buffer, 0, len);
                    out_file.flush();
                    out_file.close();
                    in.close();
                }
                size -= len;
            }
            if (socketList.containsKey(target))
                forward(msg.toString(), target);
            else {
                System.out.println(target + " msg will store to a file");
            }
        }
    }

    private void forward(String msg, String target) {
        synchronized (socketList) {

            try {
                DataOutputStream out = new DataOutputStream(socketList.get(target).getOutputStream());

                System.out.println("Target: " + target);
                System.out.println("Msg: " + msg);
                System.out.println("Socket: " + socketList.get(target));

                out.writeInt(msg.length());
                out.write(msg.getBytes(), 0, msg.length());
            } catch (IOException ex) {
                print("Unable to forward message to %s:%d\n",
                        socketList.get(target).getInetAddress().getHostName(), socketList.get(target).getPort());
            }

        }
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new Server(port);
    }
}
