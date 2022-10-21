import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class FileInfo {
	
	public static void println(String str) {
		System.out.println(str);
	}

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);

		println("Input a file/folder path and press ENTER");

		String filename = scanner.next();
		scanner.close();

		File file = new File(filename);

		Date date = new Date(file.lastModified());
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM, YYYY hh:mm:ss");
		String dateTime = dateFormat.format(date);

		println("name : " + file.getName());
		println("size (bytes) : " + file.length());
		println("exists? : " + file.exists());
		println("dir? : " + file.isDirectory());
		println("file? : " + file.isFile());
		println("modified: " + dateTime);
		println("canonical path : " + file.getCanonicalPath());
	}
}
