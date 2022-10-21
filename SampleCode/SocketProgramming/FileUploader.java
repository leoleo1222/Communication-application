import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class FileUploader {
    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        print("Input the server IP and port number:\n");
        String serverIP = scanner.nextLine();
        int port = Integer.parseInt(scanner.nextLine());

        print("Input the file path:\n");
        String filepath = scanner.nextLine();

        File file = new File(filepath);
        if (!file.exists() && file.isDirectory())
            throw new IOException("Invalid path!");

        Socket socket = new Socket(serverIP, port);

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        FileInputStream in = new FileInputStream(file);

        byte[] filename = file.getName().getBytes();
        out.writeInt(filename.length);
        out.write(filename, 0, filename.length);

        long size = file.length();
        out.writeLong(size);

        print("Uploading %s (%d bytes)", filepath, size);

        byte[] buffer = new byte[1024];
        while(size >0) {
            int len = in.read(buffer, 0, (int) Math.min(size, buffer.length));
            out.write(buffer, 0, len);
            size -= len;
            print(".");
        }
        out.flush();
        out.close();
        in.close();
        print("Complete!");
    }
}
