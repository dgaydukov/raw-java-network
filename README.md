# Raw Java Networking

### Content
* [Description](#description)
* [UDP](#udp)
    * [UDP Unicast](#udp-unicast)
    * [UDP Multicast](#udp-multicast)
* [TCP](#tcp)
    * [Java Socket](#java-socket)
    * [Java WebSocket](#java-websocket)
    * [Java HTTP](#java-http)
* [Wireshark](#wireshark)

### Description
The goal of this project is to show raw java networking, how we can manipulate network data transfer with basic protocols like TCP/UDP and send data with java. And then how we can intercept packages with wireshark and check what is inside.

### UDP
User Datagram Protocol - is connectionless communication, where packets are sent without connection being established. Datagram - the name of the packet for UDP. Sender doesn't wait for any response from acceptor, has he received the packet or not. So with UDP there is no guarantee of arrival and order.
Strictly speaking, since it's UDP there is no client & server. And if you look into code, for both client & server it's very similar. Cause they both act as client/server at the same time. They just send and receive messages from each other.

##### UDP Unicast
If you launch [UDP App](/src/main/java/com/network/raw/udp/unicast/App.java) and start wireshark, you can see that client `localhost:4444` sending messages to server `localhost:5555` and getting UDP message back.
How can you confirm that both are running as server/client. You can use network utilities like `telnet/netcat/nmap`. Below we would use netcat to connect to both UdpClient/UdpServer and send the messages
```shell
# connect to server
nc -u 127.0.0.1 5555
# send message, and get response from server
hello
server response: originalMsg=hello

# connect to client
nc -u 127.0.0.1 4444
# send message, but since client is not responding, you won't get back any response
hello
```
You can also use `nmap` to check if ports are open and app is running
```shell
# check UDP ports (you need sudo for this)
sudo nmap -sU 127.0.0.1
# response
Nmap scan report for localhost (127.0.0.1)
Host is up (0.00019s latency).
Not shown: 995 closed udp ports (port-unreach)
PORT     STATE         SERVICE
4444/udp open|filtered krb524
5555/udp open          rplay

Nmap done: 1 IP address (1 host up) scanned in 1.31 seconds
```

Here you can see captured logs from wireshark
![wireshark UDP client-server](/data/wireshark-udp-client-server.png)
Below is detailed package that was sent from client to server
![wireshark UDP client request](/data/wireshark-udp-client-request.png)
Below is response from server (since it's UDP it's not response, but just message sent from server to client)
![wireshark UDP server response](/data/wireshark-udp-server-response.png)

##### UDP Multicast
If you launch [UDP App](/src/main/java/com/network/raw/udp/multicast/App.java) and start wireshark, you can see that client `192.168.0.31:4444` sending messages to server `230.0.0.0:5555` and getting UDP message back.
In IPv4, any address between 224.0.0.0 to 239.255.255.255 can be used as a multicast address. Only those nodes that subscribe to a group receive packets communicated to the group.
Here you can see captured logs from wireshark
![wireshark UDP client-server](/data/wireshark-udp-multicast.png)
Below is detailed package that was sent from client to server
![wireshark UDP client request](/data/wireshark-udp-multicast-message.png)

##### UDP Connection Detection
Although UDP is connection-less protocol, and there is no way to track packet delivery, there is still a way to detect if endpoint (host:port) is reachable or not. If you try to send packet to unreachable endpoint, you will get back ICMP (Internet Control Message Protocol) message.
You can imitate it by disable server creation in [UDP App](/src/main/java/com/network/raw/udp/unicast/App.java). The key is to call `socket.connect(address, serverPort);`. Keep in mind if you don't call this method, java code will not handle ICMP message. From the method description of `DatagramSocket.connect` you can see that
```
 If the remote destination to which the socket is connected does not exist, or is otherwise unreachable, and if an ICMP destination unreachable packet has been received for that address, then a subsequent call to send or receive may throw a PortUnreachableException. Note, there is no guarantee that the exception will be thrown.
```
So you add code to handle `PortUnreachableException`, and by this you can understand that message wasn't delivered because endpoint is not active (no UDP application is listening on this port).
Below is how it looks like from wireshark
![wireshark UDP client-server](/data/wireshark-udp-icmp-response.png)
This is how message itself looks like
![wireshark UDP client-server](/data/wireshark-udp-icmp-message.png)

### TCP
Transmission Control Protocol - compare to UDP is reliable and connection-based. To start communication client and server need to establish connection first, and then they can keep sending messages to each other using this connection. Terminology also differs, compare to datagrams in UDP, here packets are called segments. TCP ensures that segments are delivered and ordered. If there is network congestion it does flow control. That's why TCP packet is larger than UDP, here you need to have sequence number and acknowledgement number to keep track and order of segments sent.
Don't confuse following protocols:
* Socket - raw TCP/IP contamination. When we talk about java socket - we imply that this is java abstraction on TCP, so TCP java programming is basically socket programming and based on 2 java classes `Socket/ServerSocket` from `java.net` package
* HTTP - abstraction on top of TCP with java
* WebSocket - abstraction on top of TCP
* All other communication are based on either of these 3, for example Kafka underneath using its own binary implementation on top of TCP
Here we will show java examples with all of above implementation starting with raw TCP

##### Java Socket
The term socket programming can be applied to both TCP and UDP, but usually when we say socket, we imply TCP socket. Since TCP is connection-based protocol, here we can have a proper Client and Server. Where Client connects to Server, and they start communicate. You can run our [TCP App](/src/main/java/com/network/raw/tcp/socket/App.java) and see how it works. You can connect with netcat. App is written to such a way that for each new connection it creates a separate thread where it handles it.
```shell
# connect to TCP server
nc 127.0.0.1 5555
# send message and get response
hello
server response, originalMsg=hello
```
You can check in wireshark how TCP actually differs from UDP. If you run the App, but comment client, only server.
1. Open TCP connection with netcat `nc 127.0.0.1 5555` - just open don't type anything
You will see that 4 messages in wireshark:
   * Client send `SYN` messages (syn from synchronized)
   * Server responds with `SYN, ACK` (ack from acknowledged)
   * Client sends `ACK`
This names from TCP header flags
![wireshark TCP client-server](/data/wireshark-tcp-open-connection.png)
2. Send message in netcat. You will get response from the server (this is how this app works). In wireshark you will see 4 messages
   * client send data segment to server `PSH, ACK`
   * server responds with `ACK`
   * server send response message `PSH, ACK`
   * client responds with `ACK`
![wireshark TCP messages](/data/wireshark-tcp-4-messages.png)
3. If you send data again through netcat you will see another 4 messages in wireshark
![wireshark TCP messages](/data/wireshark-tcp-4-messages-2.png)
You can notice that `Seq, Ack` keep increasing:
* Seq (Sequence number) - how many bytes sender sent to receiver:
  * for `SYN, FIN` - increasing for 1
  * for any data - increasing for number of bytes in the message
* Ack (Acknowledgement number) - how many bytes receiver already received
TCP using these 2 number to understand if some packets are lost
4. When somebody decides to close connection he sends `FIN`
![wireshark TCP close](/data/wireshark-tcp-close-connection.png)

##### Java WebSocket
* websocket is application level protocol (similar to HTTP) that runs on top of TCP
* it has its own protocol rules (just like HTTP) like handshake
* it's designed to run in the browser, so browser can connect to your application, and they can communicate with each other
* java doesn't have default implementation, but there are many third-party libraries
* `javax.websocket` was moved to `jakarta API`
Don't confuse it with plain socket which is java native implementation of TCP protocol. It's versatile and can be used anywhere.
You can use node.js utility `wscat -c ws://127.0.0.1:8080` to connect to ws and run the app.
Although `javax.websocket` defines API to build websocket server, there is no such server in java. You have to either use any third-party servers or manually setup your own server with `java.net.SocketServer` class. Otherwise, your websocket just would be class in java.
  

##### Java HTTP
* HTTP is application-level protocol that runs on top of TCP
* it's connection-less protocol, where client connect to server, get response and connection is closed
* you can implement basic HTTP protocol on top of Java sockets

### Wireshark
For better understanding of network communication you can use [wireshark](https://www.wireshark.org) app for traffic analysis. With this utility you can view network packages and analyze their data.

* rewrite tcp socket to send raw bytes
* create spring project and check how it works with wireshark
* add java basic http client/server code
* add ws code