import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client2 {
    public static String username = " "; // the username of the user
    private static String password = " "; // the password of the user

    public Client2(String serverIP, int port) throws IOException {
        System.out.printf("Connecting to %s:%d\n", serverIP, port);
        Socket socket = new Socket(serverIP, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        byte[] buffer = new byte[1024];
        String[] header = { "reg", "single", "group", "showList", "exit" };
        Scanner sc = new Scanner(System.in);
        // Hashmpa contain a groupname and a linked list obj
        // create two linked list obj to store group message and single message
        LinkedList groupMsg = new LinkedList();
        HashMap<String, LinkedList> groupList = new HashMap<String, LinkedList>();
        LinkedList singleMsg = new LinkedList();
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

                    // if the message contain Single->, then add the message to the singleMsg linked
                    // list
                    if (receive.contains("Single->")) {
                        // add the msg after the string Direct Msg->
                        singleMsg.add(receive.substring(receive.indexOf("Single->") + 8));
                    }
                    // if the message contain Single->, then add the message to the singleMsg linked list
                    // the msg with (xxx) is the group name, and we extract the groupname and put it into the hashmap with linked list
                    else if (receive.contains("Group->")) {
                        String groupname = receive.substring(receive.indexOf("Group->") + 7, receive.indexOf(")"));
                        if (groupList.containsKey(groupname)) {
                            groupList.get(groupname).add(receive.substring(receive.indexOf(")") + 1));
                        } else {
                            groupList.put(groupname, new LinkedList());
                            groupList.get(groupname).add(receive.substring(receive.indexOf(")") + 1));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
        while (true) { // sending msg
            System.out.print(
                    "Type 1 to send a direct message, 2 to send a group message, 3 to check user list, 4 to show message in group");
            int choice = sc.nextInt();
            sc.nextLine();
            if (choice == 1) {
                sendString(header[1], out);
                while (true) {
                    System.out.println("Type @@quit to quit the session");
                    // while singleMsg next() is not null then print out the next() msg
                    while (singleMsg.next() != null) {
                        System.out.println(singleMsg.next());
                    }
                    System.out.println("Enter a receiver name:");
                    String receiver = sc.nextLine();
                    if (receiver.equals("@@quit"))
                        break;
                    sendString(receiver, out);
                    System.out.println("Input message and press ENTER");
                    String message = username + ": ";
                    message += sc.nextLine();
                    if (message.equals("@@quit"))
                        break;
                    sendString(message, out);
                }
            } else if (choice == 2) {
                // create a group with member, the client will send out the member list to the
                // server
                // and the server will create a group with the member list
                sendString(header[2], out);
                // user can perform four operation: create/ join/ leave/ send with typing 1-4
                // create a String array with 4 elements
                String[] groupOperation = { "create", "join", "leave", "send", "show" };
                System.out.print(
                        "Type 1 to create a group, 2 to join a group, 3 to leave a group, 4 to chat in a group, ");
                // print type 5 to show group list
                System.out.println("Type 5 to show group list");
                int groupChoice = sc.nextInt();
                sc.nextLine();
                // send the group operation to the server
                sendString(groupOperation[groupChoice - 1], out);
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
                        if (member.equals("!end"))
                            break;
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
                    while(true){
                        System.out.println("Type @@quit to quit the session");
                        System.out.println("Enter a group name:");
                        String groupName = sc.nextLine();
                        // if the user type @@quit, then break the loop
                        if (groupName.equals("@@quit")) break;
                        sendString(groupName, out); // send the group name to the server
                        System.out.println("Input message and press ENTER");
                        String message = username + ": ";
                        message += sc.nextLine();
                        // if the user type @@quit, then break the loop
                        if (message.equals("@@quit")) break;
                        sendString(message, out); // send the message to the server
                    }
                }
            } else if (choice == 3) {
                sendString(header[3], out);
            } else if (choice == 4) {
                System.out.println("Enter a group name:");
                String groupName = sc.nextLine();
                // if the group name is not in the groupList, then print out the error msg
                if (!groupList.containsKey(groupName)) {
                    System.out.println("No such group");
                } else {
                    // if the group name is in the groupList, then print out the msg in the group
                    while (groupList.get(groupName).next() != null) {
                        System.out.println(groupList.get(groupName).next());
                    }
                }                
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

// create Linekd list class
class LinkedList {
    // create a node class
    private class Node {
        private String data;
        private Node next;

        public Node(String data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void add(String data) {
        Node node = new Node(data);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void remove(String data) {
        if (head == null)
            return;
        if (head.data.equals(data)) {
            head = head.next;
            size--;
            return;
        }
        Node prev = head;
        Node cur = head.next;
        while (cur != null) {
            if (cur.data.equals(data)) {
                prev.next = cur.next;
                size--;
                return;
            }
            prev = cur;
            cur = cur.next;
        }
    }

    public boolean contains(String data) {
        Node cur = head;
        while (cur != null) {
            if (cur.data.equals(data))
                return true;
            cur = cur.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public String next() {
        if (head == null)
            return null;
        String res = head.data;
        head = head.next;
        size--;
        return res;
    }

    public String toString() { // print out the linked list
        String res = "";
        Node cur = head;
        while (cur != null) {
            res += cur.data + " ";
            cur = cur.next;
        }
        return res;
    }
}
