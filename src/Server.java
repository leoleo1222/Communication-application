import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    // this store the online user with their socket
    HashMap<String, Socket> socketList = new HashMap<>();
    // this store their account information, we can check if the account exist with containsKey(..)
    HashMap<String, String> account = new HashMap<>();
    // id = username, in case i want to user it through method, i put it in a static var.
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
            String header = receiveString(in);
            // registration process start
            if (header.equals("reg")) {
                // get username
                String username = receiveString(in);
                // get password
                String password = receiveString(in);
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                // register the account
                if (!account.containsKey(username)) {
                    account.put(username, password);
                    sendString(password, out);
                } else {
                    sendString(account.get(username), out);
                }
                // put the username into the static var.
                id = username;
                // print out the login username in server side for debug use
                System.out.println(id + " logged in");
            }

            synchronized (socketList) {
                // let the user online
                socketList.put(id, clientSocket);
            }

            Thread t = new Thread(() -> {
                try {
                    // start passing msg with the server
                    serve(clientSocket);
                } catch (IOException ex) {
                    System.out.println("Connection drop!");
                }

                synchronized (socketList) {
                    // print out the logout username in server side for debug use
                    System.out.println(id + " is offline");
                    // let the user offline
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

        // extract data from offline file data
        Thread t = new Thread(() -> {
            try {
                for (String username : socketList.keySet()) {
                    if (new File(username + ".txt").exists() && socketList.containsKey(username)) {
                        String offline_msg = get_offline_data(new File(username + ".txt"));
                        forward(offline_msg, username, "offline");
                        if (new File(username + ".txt").delete())
                            System.out.println("Deleted " + username + "'s offline data file");
                        else
                            System.out.println("Fail to delete " + username + "'s offline data file");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        t.start();

        while (true) {
            // not necessary, just telling the client list to the client side
            // further development: client can ask for the client list with some command (input: showList)
//            int i = 1;
//            StringBuilder name_list = new StringBuilder();
//            for (String username : socketList.keySet()) {
//                name_list.append("[").append(i++).append("]").append(username).append("\n");
//            }
//            out.writeInt(name_list.length());
//            out.write(name_list.toString().getBytes(), 0, name_list.length());
            String type = "";
            int size = in.readInt();
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                type += new String(buffer, 0, len);
                size -= len;
            }
            // receiving the msg target from the client
            String target = "";
            size = in.readInt();
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                target += new String(buffer, 0, len);
                size -= len;
            }
            // receiving the msg from the client
            StringBuilder msg = new StringBuilder("");
            size = in.readInt();
            while (size > 0) {
                int len = in.read(buffer, 0, Math.min(size, buffer.length));
                msg.append(new String(buffer, 0, len));
                if (!socketList.containsKey(target) && account.containsKey(target)) {
                    File file = new File(target + ".txt");
                    FileOutputStream out_file = new FileOutputStream(file, true);
                    System.out.println("Saved into " + target);
                    out_file.write(buffer, 0, len);
                    out_file.write('\n');
                    out_file.flush();
                    out_file.close();
                }
                size -= len;
            }

            if (socketList.containsKey(target))
                forward(msg.toString(), target, type);
            else {
                // This is a debug msg, it will show when the receiver is offline
                if (account.containsKey(target)) System.out.println(target + " msg will store to a file");
            }
        }
    }

    private void forward(String msg, String target, String type) {
        synchronized (socketList) {

            try {
                // the socket list will be the target socket == socketList.get(target)
                DataOutputStream out = new DataOutputStream(socketList.get(target).getOutputStream());
                // msg show in server side telling the msg detail in each time client forwarding msg
                System.out.println("Type: " + type);
                System.out.println("Target: " + target);
                System.out.println("Msg: " + msg);
                System.out.println("Socket: " + socketList.get(target));
                // send the msg
                out.writeInt(msg.length());
                out.write(msg.getBytes(), 0, msg.length());
            } catch (IOException ex) {
                print("Unable to forward message to %s:%d\n",
                        socketList.get(target).getInetAddress().getHostName(), socketList.get(target).getPort());
            }

        }
    }

    public void sendString(String str, DataOutputStream out) {
        try {
            // send the header to the server
            int size = str.length();
            out.writeInt(size);
            out.write(str.getBytes(), 0, size);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String get_offline_data(File file) {
        String res = "";
        try {
            FileInputStream in = new FileInputStream(file);
            long size = file.length();
            byte[] buffer = new byte[1024];

            while (size > 0) {
                int len = in.read(buffer, 0, buffer.length);
                size -= len;
                res += new String(buffer, 0, len);
            }

            in.close();
            return res;
        } catch (Exception e) {
            System.out.println("Error in getting file data");
        }
        return res;
    }

    public String receiveString(DataInputStream in) {
        String res = "";
        try {
            byte[] buffer = new byte[1024];
            int len = in.readInt();
            while (len > 0) {
                int l = in.read(buffer, 0, Math.min(len, buffer.length));
                res += new String(buffer, 0, l);
                len -= l;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new Server(port);
    }
}
