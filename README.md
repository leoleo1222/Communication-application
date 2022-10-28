# Networking-project

## Client sending message
```java
String header = "sth";
int header_size = header.length();
out.writeInt(header_size);
out.write(header.getBytes(), 0, header_size);
```
##Server receiving message
```java
Socket clientSocket = srvSocket.accept();
DataInputStream in = new DataInputStream(clientSocket.getInputStream());
byte[] buffer = new byte[1024];
String header = "";
int size = in.readInt();
while (size > 0) {
	int len = in.read(buffer, 0, Math.min(size, buffer.length));
	header += new String(buffer, 0, len);
	size -= len;
}
```
