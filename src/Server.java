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
    HashMap<String, ArrayList<String>> group = new HashMap<>();

    private static String id = "";

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public Server(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);

        while (true) {
            print("Listening at port %d...\n", port);
            Socket clientSocket = srvSocket.accept();
            try {
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
            } catch (Exception e) {
                continue;
            }


            synchronized (socketList) {
                // let the user online
                socketList.put(id, clientSocket);
            }

            Thread t = new Thread(() -> {
                try {
                    // start passing msg with the server
                    serve(id, clientSocket);
                } catch (IOException ex) {
                    System.out.println("Connection drop!\n");
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

    private void serve(String name, Socket clientSocket) throws IOException {
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
            int i = 1;
            StringBuilder name_list = new StringBuilder();
<<<<<<< Updated upstream
            for (String username : socketList.keySet()) {
                name_list.append("[").append(i++).append("]").append(username).append("\n");
=======
            for (String username : account.keySet()) {
                name_list.append(",").append(username);
>>>>>>> Stashed changes
            }
//            out.writeInt(name_list.length());
//            out.write(name_list.toString().getBytes(), 0, name_list.length());
            // receive the msg type from client
            String type = receiveString(in);
            // if the msg type is "single", then it is a normal msg from client to client
            if (type.equals("single")) {
                // receiving the msg target from the client
                String target = receiveString(in);
                // receiving the msg from the client
                StringBuilder msg = new StringBuilder("");
                int size = in.readInt();
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

                if (socketList.containsKey(target)) {
//                forward(name_list.toString(), target, "System msg");
                    forward(msg.toString(), target, type);
                } else {
                    // This is a debug msg, it will show when the receiver is offline
                    if (account.containsKey(target)) System.out.println(target + " msg will store to a file");
                }
            }
            if (type.equals("group")) {
                String action = receiveString(in);
                if (action.equals("create")) { // create a group
                    String group_name = receiveString(in);
                    group.put(group_name, new ArrayList<>());
                    String member = receiveString(in);
                    while (!member.equals("!end")) {
                        // check if the member exist
                        if (account.containsKey(member)) {
                            group.get(group_name).add(member);
                        } else {
                            System.out.println(member + " is not exist");
                        }
                        member = receiveString(in);
                    }
                    // print the member list in the created group
                    System.out.println("Group " + group_name + " is created with members:");
                    for (String member_name : group.get(group_name)) {
                        System.out.println(member_name);
                    }
                    // send a msg to the creator to tell him/her the group is created
                    sendString("System: " + "Group " + group_name + " is created by " + name, out);
                }
                if (action.equals("join")) {  // join a group
                    String group_name = receiveString(in);
                    if (group.containsKey(group_name)) {
                        group.get(group_name).add(id);
                        sendString("System: " + "You joined group " + group_name, out);

                    } else {
                        sendString("System: " + "Group " + group_name + " does not exist", out);
                    }
                }
                if (action.equals("leave")) { // leave a group
                    String group_name = receiveString(in);
                    if (group.containsKey(group_name)) {
                        group.get(group_name).remove(id);
                        sendString("System: " + "You left group " + group_name, out);

                    } else {
                        sendString("System: " + "Group " + group_name + " does not exist", out);

                    }
                }
                if (action.equals("send")) {  // send msg to a group
                    String group_name = receiveString(in);
                    if (group.containsKey(group_name)) {
                        String msg = receiveString(in);
                        for (String member : group.get(group_name)) {
                            forward(msg, member, "group");
                        }
                    } else {
                        sendString("System: " + "Group " + group_name + " does not exist", out);
                    }
                }
                if (action.equals("show")) {  // show the group list
                    StringBuilder group_list = new StringBuilder();
                    group_list.append("Group list:\n");
                    for (String group_name : group.keySet()) {
                        group_list.append(group_name).append("\n");
                    }
                    sendString(group_list.toString(), out);
                }

            }
            if (type.equals("showList")) {    // show the client list
                sendString("System: " + name_list.toString(), out);
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
                System.out.println("Socket: " + socketList.get(target) + "\n");
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

    public String receiveString(DataInputStream in) throws IOException {
        String res = "";
//        try {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        while (len > 0) {
            int l = in.read(buffer, 0, Math.min(len, buffer.length));
            res += new String(buffer, 0, l);
            len -= l;
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new Server(port);
    }
}