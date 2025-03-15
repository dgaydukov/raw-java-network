# Raw Java Networking

### Content
* [Description](#description)
* [UDP](#udp)
* [TCP](#tcp)
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


### TCP
Transmission Control Protocol - compare to UDP is reliable and connection-based. To start communication client and server need to establish connection first and then they can keep sending messages to each other using this connection. Terminology also differs, compare to datagrams in UDP, here packets are called segments. TCP ensures that segments are delivered and ordered. If there is network congestion it do flow control. That's why TCP packet is larger than UDP, here you need to have sequence number and acknowledgement number to keep track and order of segments sent.

add this code `socket.connect(address, 6666);` and icmp error
https://stackoverflow.com/questions/33260478/udp-in-java-thinks-that-udp-has-connections



What is the difference between 3:
TCP/IP
Socket
WebSocket
HTTP
Kafka

### Wireshark
For better understanding of network communication you can use [wireshark](https://www.wireshark.org) app for traffic analysis. With this utility you can view network packages and analyze their data.