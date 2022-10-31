import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextSaver {

    public static void println(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) throws IOException {

        println("Enter the file name and Press ENTER");

        InputStream in = System.in;
        byte[] buffer = new byte[1024];
        int len = in.read(buffer, 0, buffer.length);

        String filename = new String(buffer, 0, len);
        filename = filename.replace("\n", "").replace("\r", "");

        File file = new File(filename);

        println("Please enter the content. (enter @@quit in a new line to quit)");

        FileOutputStream out = new FileOutputStream(file);

        while (true) {
            len = in.read(buffer, 0, buffer.length);
            String str = new String(buffer, 0, len);
            if (str.contains("@@quit"))
                break;

            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
        in.close();

        println("bye");
    }

}
