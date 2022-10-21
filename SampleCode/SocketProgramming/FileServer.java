import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    public static void print(String str, Object... o) {
        System.out.printf(str, o);
    }

    public FileServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            print("Listening to port %d...\n", port);
            Socket socket = serverSocket.accept();
            Thread t = new Thread(() -> {
                try {
                    serve(socket);
                } catch (IOException ex) {

                } finally {
                    print("Connection dropped!\n");
                }
            });
            t.start();
        }
    }

    private void serve(Socket clientSocket) throws IOException {
        String remoteIP = clientSocket.getInetAddress().getHostAddress();
        int remotePort = clientSocket.getPort();
        print("Connected to %s:%d!\n", remoteIP, remotePort);
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());

        byte[] buffer = new byte[1024];
        int remain = in.readInt();
        String filename = "";
        while(remain > 0) {
            int len = in.read(buffer, 0, Math.min(remain, buffer.length));
            filename += new String(buffer, 0, len);
            remain -= len;
        }

        File file = new File(System.currentTimeMillis() + "_" + filename);
        FileOutputStream fout = new FileOutputStream(file);

        long size = in.readLong();

        print("Downloading %s (%d bytes) from %s:%d...\n", filename, size, remoteIP, remotePort);

        while(size > 0) {
            int len = in.read(buffer, 0, (int) Math.min(size, buffer.length));
            fout.write(buffer, 0, len);
            size -= len;
            print(".");
        }
        print("Completed!\n");
        fout.flush();
        fout.close();
        in.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        new FileServer(5556);
    }
}
