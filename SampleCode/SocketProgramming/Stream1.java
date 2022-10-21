import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Stream1 {
	public static void main(String[] args) throws IOException {
		InputStream in = System.in;
		OutputStream out = System.out;

		byte[] buffer = new byte[10];

		int count = 0;
		
		System.out.println("Input something and press ENTER");

		while(true) {

			int len = in.read(buffer, 0, buffer.length);

			out.write(buffer, 0, len);

			System.out.println(++count);
		}

	}

}
