import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Stream3 {

	public static void println(String str) { System.out.println(str); }
	public static void printf(String str, Object...o) { System.out.printf(str, o); }
	
	public static void main(String[] args) throws IOException {
		InputStream iStream = System.in;
		OutputStream oStream = (OutputStream) System.out;
		
		DataInputStream dataIn = new DataInputStream(iStream);
		DataOutputStream dataOut = new DataOutputStream(oStream);
		
		
		println("Input 8 digits and press ENTER");

		byte[] buffer = new byte[4];
		dataIn.read(buffer, 0, 4);
		
		int num1 = Integer.parseInt(new String(buffer));
		int num2 = dataIn.readInt();
			
		printf("\nThe retrieved inputs are %d and %d respectively.", 
				num1, num2);
			
		println("\n\nOutput using the write() method of DataOutputStream: ");
		dataOut.write(buffer);		
		
		println("\n\nOutput using the writeInt() method of DataOutputStream: ");		
		dataOut.writeInt(num2);		

		dataOut.flush();
		
		dataIn.close();
		dataOut.close();
		
		iStream.close();
		oStream.close();
		
	}
}
