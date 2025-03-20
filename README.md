# Raw Java Networking

### Content
* [Description](#description)
* [UDP](#udp)
    * [UDP Unicast](#udp-unicast)
    * [UDP Multicast](#udp-multicast)
    * [UDP Connection Detection](#udp-connection-detection)
    * [UDP NIO](#udp-nio)
* [TCP](#tcp)
    * [Java Socket](#java-socket)
    * [Java WebSocket](#java-websocket)
    * [Java HTTP](#java-http)
    * [TCP NIO](#tcp-nio)
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

##### UDP NIO
You can check how to build UDP client/server connection using `java.net`. But this maybe not the optimal solution, because you need to run loop with `while (true)` to constantly check if there is new message from client. Especially this is true to TCP sockets, cause here you create separate thread for each new client. That's why java provided new networking API in `java.nio`, with classes like `DatagramChannel` you can create client/server, but without endless loops. This class supports "multiplexed-wait", so you can ask "is there a packet for me on any of the existing port". But without it you have to use thread-per-port. So the advantage:
* for UDP - for server you have to use thread-per-port, but with channel you can use multiple port in single thread
* for TCP - you have to use thread-per-client, but with channels you can handle all clients in the same thread
If you open code [ChannelUdpServer](/src/main/java/com/network/raw/udp/nio/ChannelUdpServer.java) you will notice that it's also blocking. That's why it's working just as usual `UdpServer`, it waits for messages, so this line
```
SocketAddress address = server.receive(buffer);
```
it waits until it receives message. Because by default NIO is configured to be blocking. But if you add this code
```
channel.configureBlocking(false);
```
Your code would be turned into non-blocking. And the line above to receive message, will not wait for incoming message. If there are no incoming messages, it would return immediately `null`. Same with client. In my opinion it's better to use blocking, because code is simpler, but if your code doesn't need to wait, you can turn on this feature.

### UDP datagram size and MTU
By default, the size of UDP datagram is 65K, which means you can send messages with length up to 65000. But if you try to send bigger message, your `send` method would throw `java.io.IOException: Message too long`. But below this limit, we can send UDP datagram and even check it in wireshark
![wireshark UDP 65k packet](/data/wireshark-udp-65k-packet.png)
But MTU is still 1500 bytes. How this possible? Sending over localhost you send jumbo frames, but in real-world Ethernet you will send frames with size 1500, and for real-world apps, you need to keep in mind that your payload (datagram size) shouldn't exceed 1500. Yet if your packet would exceed MTU, then IP protocol under-the-hood would break your payload into fragments and reassemble. But for UDP it's a problem, what if some fragment won't arrive, then whole payload would be incorrectly reassembled. For this use-case, it's always better to use payload size compatible with MTU size.
So to summarize, UDP doesn't do fragmentation, it can send any message up to 65K. But below UDP we have IP protocol, which depending upon network, will fragment the underlying packet into multiple fragments if original size exceed MTU (for Ethernet MTU is 1500). After fragmentation every fragment is a packet of its own (have its own IP header) and send separately. That's the problem with UDP, cause we would have instead of one large UDP, many fragments would be sent, and some may get lost.

### TCP
Transmission Control Protocol - compare to UDP is reliable and connection-based. To start communication client and server need to establish connection first, and then they can keep sending messages to each other using this connection. Terminology also differs, compare to datagrams in UDP, here packets are called segments. TCP ensures that segments are delivered and ordered. If there is network congestion it does flow control. That's why TCP packet is larger than UDP, here you need to have sequence number and acknowledgement number to keep track and order of segments sent.
Don't confuse following protocols:
* Socket - raw TCP/IP contamination. When we talk about java socket - we imply that this is java abstraction on TCP, so TCP java programming is basically socket programming and based on 2 java classes `Socket/ServerSocket` from `java.net` package
* HTTP - abstraction on top of TCP with java
* WebSocket - abstraction on top of TCP
* All other communication are based on either of these 3, for example Kafka underneath using its own binary implementation on top of TCP
Here we will show java examples with all above implementation starting with raw TCP.
TCP retransmission - TCP ensures packet deliver, and use the concept of timer. Once sender sends data, it starts timer:
* if sender gets ACK (receiver has received data and send ACK) then timer is expired
* if sender doesn't get ACK and timer is up, sender re-send packet and create new timer
Ad you see TCP on the protocol level ensures delivery and order of packets.

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

##### TCP NIO
* selector - monitors many channels and understand when a channel is available for data transfers
* with selector - single thread can be used to manage multiple channels
* for TCP socket - we have to create new thread to each connected client, [TCP Socket](/src/main/java/com/network/raw/tcp/socket/App.java), but with selector we can handle all clients in a single thread
* we register multiple channels in single selector - when I/O happens, selector notify us
* `FileChannels` can't be used with selector, because it can't be switched into non-blocking mode, like socket channels
Conclusion: NIO for UDP is oversold, you don't need it, because in UDP every messages is self-contained request. Yet for TCP, NIO with selectors is really a good choice, because in case of TCP you can handle multiple clients all in a single thread.

##### Java WebSocket
* websocket is application level protocol (similar to HTTP) that runs on top of TCP
* it has its own protocol rules (just like HTTP) like handshake
* it's designed to run in the browser, so browser can connect to your application, and they can communicate with each other
* java doesn't have default implementation, but there are many third-party libraries
* `javax.websocket` was moved to `jakarta API`
Don't confuse it with plain socket which is java native implementation of TCP protocol. It's versatile and can be used anywhere.
You can use node.js utility `wscat -c ws://127.0.0.1:8080/ws` to connect to ws and run the app.
Although `javax.websocket` defines API to build websocket server, there is no such server in java. You have to either use any third-party servers or manually setup your own server with `java.net.SocketServer` class. Otherwise, your websocket just would be class in java.
Websocket under-the-hood works on top of TCP, and same way open TCP connection and sends messages.
1. connect to ws `wscat -c ws://127.0.0.1:8080/ws`. Here you see multiple message for opening connection
![wireshark websocket](/data/wireshark-websocket-connect.png)
2. send message and get response - here you see 4 messages to send text to server, ACK, server response and client ACK
![wireshark websocket](/data/wireshark-websocket-send-message.png)
3. close connection - here you see multiple TCP messages
![wireshark websocket](/data/wireshark-websocket-disconnect.png)

##### Java HTTP
Look into [TCP Channel](/src/main/java/com/network/raw/tcp/nio/App.java) to see how it can handle multiple clients in the single thread.
* HTTP is application-level protocol that runs on top of TCP
* it's connection-less protocol, where client connect to server, get response and connection is closed
* you can implement basic HTTP protocol on top of Java sockets
You can setup spring boot project which has built-in web-server. And start the app. You can use `curl` to call HTTP API. If you call API `curl http://127.0.0.1:8080/users` and check wireshark you will see.
![wireshark HTTP](/data/wireshark-http-get-request.png)
Since HTTP is standard protocol, wireshark display it as so, but if you look close, under-the-hood it's just simple TCP call with `PSH, ACK` flag, both HTTP request and response. Pay attention that after getting response, client send `FIN` to close TCP connection. So if you look closely into `HTTP` response, it's basically TCP push message
![wireshark HTTP](/data/wireshark-http-server-response.png)

### Wireshark
For better understanding of network communication you can use [wireshark](https://www.wireshark.org) app for traffic analysis. With this utility you can view network packages and analyze their data.