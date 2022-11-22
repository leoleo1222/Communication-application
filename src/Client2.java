import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Client2 {
    public static String username = " "; // the username of the user
    private static String password = " "; // the password of the user
    private static HashMap<String, Stack<String>> singleMsg = new HashMap<>(); // the single msg
    private static HashMap<String, Stack<String>> groupMsg = new HashMap<>(); // the group msg

    public Client2(String serverIP, int port) throws IOException {
        System.out.printf("Connecting to %s:%d\n", serverIP, port);
        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[1024];
        String[] header = {"reg", "single", "group", "showList", "exit"};
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("Registration/Login");
            // input the username
            System.out.println("Input username:");
            username = sc.nextLine();
            // input the password
            System.out.println("Input password:");
            password = sc.nextLine();
            // send the header as reg, it means the sending msg is a registration msg
            sendString(header[0], out);
            sendString(username, out);
            sendString(password, out);
        } while (!receiveString(in).equals(password));
        Thread t = new Thread(() -> {
            try {
                while (true) { // receiving msg
                    String receive = "";
                    int r_size = in.readInt();
                    while (r_size > 0) {
                        int len = in.read(buffer, 0, Math.min(r_size, buffer.length));
                        receive += new String(buffer, 0, len);
                        r_size -= len;
                    }
                    // if the receive msg contain Single->, it means the msg is a single msg and add it to the singleMsg
                    // the xxx: is the sender name
                    if (receive.contains("Single->")) {
                        String sender = receive.substring(8, receive.indexOf(":"));
                        if (singleMsg.containsKey(sender)) {
                            // push the msg after Single-> 
                            singleMsg.get(sender).push(receive);
                        } else {
                            Stack<String> stack = new Stack<>();
                            stack.push(receive);
                            singleMsg.put(sender, stack);
                        }
                    }
                    // if the receive msg contain Group->, it means the msg is a group msg and add it to the groupMsg
                    // the (xxx) is the group name
                    else if (receive.contains("Group->")) {
                        String group = receive.substring(8, receive.indexOf(")"));
                        if (groupMsg.containsKey(group)) {
                            // push the msg after Group-> 
                            groupMsg.get(group).push(receive);                  
                        } else {
                            Stack<String> stack = new Stack<>();
                            stack.push(receive);
                            groupMsg.put(group, stack);
                        }
                    }else
                        System.out.println(receive);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
        while (true) { // sending msg
            System.out.print("Type 1 to send a direct message, 2 to group function, 3 to check user list, 4 to view direct message, 5 to view group message, 6 to exit");
            System.out.println(". Type @@quit to quit the session");
            int choice = sc.nextInt();
            sc.nextLine();
            if (choice == 1) {
                sendString(header[1] , out);
                while (true){
                    System.out.println("Enter a receiver name:");
                    String receiver = sc.nextLine();
                    if (receiver.equals("@@quit")) break;
                    sendString(receiver, out);
                    System.out.println("Input message and press ENTER");
                    String message = username+": ";
                    message += sc.nextLine();
                    if (message.equals("@@quit")) break;
                    sendString(message, out);
                }
            } else if (choice == 2) {
                // create a group with member, the client will send out the member list to the server
                // and the server will create a group with the member list
                sendString(header[2], out);
                // user can perform four operation: create/ join/ leave/ send with typing 1-4
                // create a String array with 4 elements
                String[] groupOperation = {"create", "join", "leave", "send", "show"};
                System.out.print("Type 1 to create a group, 2 to join a group, 3 to leave a group, 4 to send a message, ");
                // print type 5 to show group list
                System.out.println("Type 5 to show group list");
                int groupChoice = sc.nextInt();
                sc.nextLine();
                // send the group operation to the server
                sendString(groupOperation[groupChoice-1], out);
                if (groupChoice == 1) { // create a group
                    // send out the creator name direct message, 2 to send a group messa
                    System.out.println("Enter the group name:");
                    String groupName = sc.nextLine();
                    sendString(groupName, out);
                    // user can keep adding member to the group until the user type !end
                    while (true) {
                        System.out.println("Enter a member name: (Enter !end to end)");
                        String member = sc.nextLine();
                        sendString(member, out);
                        if (member.equals("!end")) break;
                    }
                }
                if (groupChoice == 2) { // join a group
                    System.out.println("Enter a group name:");
                    String groupName = sc.nextLine();
                    sendString(groupName, out); // send the group name to the server
                    sendString(username, out); // send the username to the server
                }
                if (groupChoice == 3) { // leave a group
                    System.out.println("Enter a group name:");
                    String groupName = sc.nextLine();
                    sendString(groupName, out); // send the group name to the server
                    sendString(username, out); // send the username to the server
                }
                if (groupChoice == 4) { // send a message to a group
                    System.out.println("Enter a group name:");
                    String groupName = sc.nextLine();
                    sendString(groupName, out); // send the group name to the server
                    System.out.println("Input message and press ENTER");
                    String message = username+": ";
                    message += sc.nextLine();
                    sendString(message, out); // send the message to the server
                }
            }else if (choice == 3) {
                sendString(header[3], out);
            }
            else if (choice == 4) {
                System.out.println("Received direct messages from:");
                // list out all the sender name
                for (String sender : singleMsg.keySet()) {
                    System.out.println(sender);
                }
                System.out.println("Enter a sender name to view the message:");
                String sender = sc.nextLine();
                // print out all the message from the sender
                while (!singleMsg.get(sender).isEmpty() && singleMsg.containsKey(sender)) {
                    String message = singleMsg.get(sender).pop();
                    System.out.println(message.substring(8));
                }

            } else if (choice == 5) {
                System.out.println("Received group messages from:");
                // list out all the group name
                for (String group : groupMsg.keySet()) {
                    System.out.println(group);
                }
                System.out.println("Enter a group name to view the message:");
                String group = sc.nextLine();
                // print out all the message from the group
                while (!groupMsg.get(group).isEmpty() && groupMsg.containsKey(group)) {
                    String message = groupMsg.get(group).pop();
                    System.out.println(message.substring(7));
                }
                
            } else if (choice == 6) {
                System.out.println("Program terminated");
                System.exit(0);
            }
        }
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

    public static void main(String[] args) {

        String serverIP = "127.0.0.1";
        int port = 12345;
        try {
            new Client2(serverIP, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
