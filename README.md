# Networking-project

                    
###### Java class
                    
Class  | Description
------------- | -------------
Server  | Help Client to store and pass message
Client  | Register account and sending/uploading message 

##### The flow of Client
```flow
reg=>start: Registration
st=>start: Login
op=>operation: Login operation
cond_r=>condition: Successful Yes or No?
cond=>condition: Successful Yes or No?
e=>end: Start sending message

reg->cond_r->st->op->cond
cond(yes)->e
cond(no)->op
cond_r(yes)->st
cond_r(no)->reg
```

###### Client sending message
```java
String header = "header";
int header_size = header.length();
out.writeInt(header_size);
out.write(header.getBytes(), 0, header_size);
```
###### Server receiving message
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
