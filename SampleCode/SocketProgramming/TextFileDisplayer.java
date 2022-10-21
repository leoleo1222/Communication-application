import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class TextFileDisplayer {

	public static void println(String str) {
		System.out.println(str);
	}
	
	public static void main(String[] args) throws IOException {
		
		println("Input a text file path and press ENTER");
		
		Scanner scanner = new Scanner(System.in);
		String filename = scanner.next();
		
		byte[] buffer = new byte[1024];

		File file = new File(filename);
		FileInputStream in = new FileInputStream(file);
		long size = file.length();

		while (size > 0) {
			int len = in.read(buffer, 0, buffer.length);
			size -= len;

			println(new String(buffer, 0, len));
		}
		
		scanner.close();
		in.close();
	}
}
