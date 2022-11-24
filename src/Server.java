import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    // this store the online user with their socket
    HashMap<String, Socket> socketList = new HashMap<>();
    // this store their account information, we can check if the account exist with
    // containsKey(..)
    HashMap<String, String> account = new HashMap<>();
    HashMap<String, ArrayList<String>> group = new HashMap<>();

    private static String id = "";

    public void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public Server(int port) throws IOException {
        ServerSocket srvSocket = new ServerSocket(port);
        //create 3 accounts & 1 group at the beginning for testing
        account.put("leo","leo");
        account.put("jason","jason");
        account.put("sam","sam");
        group.put("default",new ArrayList<>());

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
                String name = id;
                try {
                    // start passing msg with the server
                    serve(id, clientSocket);
                } catch (IOException ex) {
                    System.out.println("Connection drop!\n");
                }

                synchronized (socketList) {
                    // print out the logout username in server side for debug use
                    System.out.println(name + " is offline");
                    // let the user offline
                    socketList.remove(name);
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
                        String[] offline_msg = get_offline_data(new File(username + ".txt"));
                        for (String msg : offline_msg) {
                            // get the msg, the msg is after the ":"
                            String messageDetail = msg.substring(msg.indexOf(":") + 2);
                            if (messageDetail.startsWith("!file")) {
                                forward(messageDetail, username, "offline"); // forward the msg to the target
                                File file = new File(username + "/" + messageDetail.substring(6));
                                FileInputStream fin = new FileInputStream(file); // create a file input stream
                                byte[] filename = file.getName().getBytes(); // get the file name
                                out.writeInt(filename.length); // send the file name length to the server
                                out.write(filename, 0, filename.length); // send the file name to the server
                                long fsize = file.length(); // get the file fsize
                                out.writeLong(fsize); // send the file fsize to the server
                                System.out.printf("Uploading %s (%d bytes)", messageDetail.substring(6), fsize);
                                while (fsize > 0) {
                                    int len = fin.read(buffer, 0, (int) Math.min(fsize, buffer.length));
                                    out.write(buffer, 0, len); // send the file to the server
                                    fsize -= len; // update the file size
                                    System.out.printf("."); // print out a dot
                                }
                                System.out.println("Complete!"); // print out complete
                                out.flush(); // flush the output stream
                                fin.close();
                            } else
                                forward(msg, username, "offline");
                        }
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
            int i = 1;
            StringBuilder name_list = new StringBuilder();
            for (String username : account.keySet()) {
                name_list.append(",").append(username);
            }
            // receive the msg type from client
            String type = receiveString(in);
            // if the msg type is "single", then it is a normal msg from client to client
            if (type.equals("single")) {
                // receiving the msg target from the client
                String target = receiveString(in);
                // receiving the msg from the client
                StringBuilder msg = new StringBuilder("Single->");
                int size = in.readInt();
                while (size > 0) {
                    int len = in.read(buffer, 0, Math.min(size, buffer.length));
                    msg.append(new String(buffer, 0, len));
                    if (!socketList.containsKey(target) && account.containsKey(target)) {
                        File file = new File(target + ".txt");
                        FileOutputStream out_file = new FileOutputStream(file, true);
                        System.out.println("Saved into " + target);
                        // write the Single-> into the offline file
                        out_file.write("Single->".getBytes());
                        out_file.write(buffer, 0, len);
                        out_file.write('\n');
                        out_file.flush();
                        out_file.close();
                    }
                    size -= len;
                }
                // get the username, the username is between "Single->" and ":"
                String username = msg.substring(8, msg.indexOf(":"));
                // get the msg, the msg is after the ":"
                String messageDetail = msg.substring(msg.indexOf(":") + 1);
                System.out.println("username->" + username);
                System.out.println("messageDetail->" + messageDetail);
                // if the messageDetail start with !file then it is a file transfer
                // the format is !file:filename
//                if (messageDetail.startsWith("!file")) {
//                    forward("download", target, type); // forward the msg to the target
//                    File file = new File(username + "/" + messageDetail.substring(6));
//                    FileInputStream fin = new FileInputStream(file); // create a file input stream
//                    byte[] filename = file.getName().getBytes(); // get the file name
//                    out.writeInt(filename.length); // send the file name length to the server
//                    out.write(filename, 0, filename.length); // send the file name to the server
//                    long fsize = file.length(); // get the file fsize
//                    out.writeLong(fsize); // send the file fsize to the server
//                    System.out.printf("Uploading %s (%d bytes)", messageDetail.substring(6), fsize); // print out the
//                    // file name and
//                    // fsize
//                    while (fsize > 0) {
//                        int len = fin.read(buffer, 0, (int) Math.min(fsize, buffer.length)); // read the file
//                        out.write(buffer, 0, len); // send the file to the server
//                        fsize -= len; // update the file size
//                        System.out.printf("."); // print out a dot
//                    }
//                    System.out.println("Complete!"); // print out complete
//                    out.flush(); // flush the output stream
//                    fin.close();
//                }
                if (socketList.containsKey(target)) {
                    forward(msg.toString(), target, type); // forward the msg to the target
                } else {
                    // This is a debug msg, it will show when the receiver is offline
                    if (account.containsKey(target))
                        System.out.println(target + " msg will store to a file");
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
                if (action.equals("join")) { // join a group
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
                if (action.equals("send")) { // send msg to a group
                    String group_name = receiveString(in);
                    if (group.containsKey(group_name)) {
                        String msg = "Group->" + group_name + ":";
                        msg += receiveString(in);
                        // the sender name is between ) and :
                        String sender = msg.substring(msg.indexOf(")") + 1, msg.indexOf(":"));
                        for (String member : group.get(group_name)) {
                            if (!socketList.containsKey(member) && account.containsKey(member)) {
                                FileWriter fw = new FileWriter(member+".txt", true);
                                fw.append(member);
                                fw.close();
                            }
                            if (!member.equals(sender)) {
                                forward(msg, member, "single");
                            }
                        }
                    } else {
                        sendString("System: " + "Group " + group_name + " does not exist", out);
                    }
                }
                if (action.equals("show")) { // show the group list
                    StringBuilder group_list = new StringBuilder();
                    group_list.append("Group list:\n");
                    for (String group_name : group.keySet()) {
                        group_list.append(",").append(group_name);
                    }
                    sendString("System: " + group_list.toString(), out);
                }

            }
            if (type.equals("showList")) { // show the client list
                sendString("System: " + name_list.toString(), out);
            }
            if (type.equals("upload")) { // exit the server
                // get the sender name
                String target = receiveString(in); // target
                String sender = receiveString(in); // sender
                if (new File(sender).mkdir())
                    System.out.println("Created " + sender + " dir");
                int remain = in.readInt(); // the size of the file
                String filename = ""; // the name of the file
                while (remain > 0) { // receive the file name
                    int len = in.read(buffer, 0, Math.min(remain, buffer.length)); // read the file name
                    filename += new String(buffer, 0, len); // append the file name
                    remain -= len; // update the remain size
                }
                // create a file with the name inside the sender folder
                File file = new File(sender + "/" + filename);
                FileOutputStream fout = new FileOutputStream(file); // create a file output stream
                long size = in.readLong(); // read the file size
                System.out.printf("Downloading %s (%d bytes) ...\n", filename, size); // print the file name and size
                while (size > 0) { // receive the file
                    int len = in.read(buffer, 0, (int) Math.min(size, buffer.length)); // read the file
                    fout.write(buffer, 0, len); // write the file
                    size -= len; // update the remain size
                    System.out.printf("."); // print a dot to show the progress
                }
                System.out.printf("Completed!\n"); // print the complete msg
                forward("download", target, "file transfer");
                upload(file, out);
                fout.flush(); // flush the file output stream
                fout.close(); // close the file output stream
            }
        }
    }

    private void upload(File path, DataOutputStream out){
        try {
            byte[] buffer = new byte[1024];
            File file = path; // create a file object
            FileInputStream fin = new FileInputStream(file); // create a file input stream
            byte[] fileName = file.getName().getBytes(); // get the file name
            out.writeInt(fileName.length); // send the file name length to the server
            out.write(fileName, 0, fileName.length); // send the file name to the server
            long size = file.length(); // get the file size
            out.writeLong(size); // send the file size to the server
            System.out.printf("Uploading %s (%d bytes)", path, size); // print out the file name and size
            while (size > 0) {
                int len = fin.read(buffer, 0, (int) Math.min(size, buffer.length)); // read the file
                out.write(buffer, 0, len); // send the file to the server
                size -= len; // update the file size
                System.out.printf("."); // print out a dot
            }
            System.out.println("Complete!"); // print out complete
            out.flush(); // flush the output stream
            fin.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void forward(String msg, String target, String type) {
        synchronized (socketList) {

            try {
                // the socket list will be the target socket == socketList.get(target)
                DataOutputStream out = new DataOutputStream(socketList.get(target).getOutputStream());
                // msg show in server side telling the msg detail in each time client forwarding
                // msg
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

    private String[] get_offline_data(File file) throws FileNotFoundException {
        ArrayList<String> list = new ArrayList<>();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            list.add(sc.nextLine());
        }
        // return list as a string array
        return list.toArray(new String[list.size()]);
    }

    public String receiveString(DataInputStream in) throws IOException {
        String res = "";
        // try {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        while (len > 0) {
            int l = in.read(buffer, 0, Math.min(len, buffer.length));
            res += new String(buffer, 0, l);
            len -= l;
        }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        return res;
    }

    public static void main(String[] args) throws IOException {
        int port = 12345;
        new Server(port);
    }
}
