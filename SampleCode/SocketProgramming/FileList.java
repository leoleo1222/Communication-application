import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileList {

	public static void println(String str) {
		System.out.println(str);
	}
	
	public static void main(String[] args) throws IOException {
		println("Input a folder path and press ENTER");
		Scanner scanner = new Scanner(System.in);
		File dir = new File(scanner.next());

		if (dir.isDirectory()) {
			println("PATH: " + dir.getCanonicalPath());

			for (File file : dir.listFiles()) {
				char type = file.isDirectory() ? 'd':'f';
				String info = String.format("(%c) %s, %d b", type, file.getName(), file.length());
				println(info);
			}
		}
		scanner.close();
	}

}
