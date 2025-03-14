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

### Wireshark
For better understanding of network communication you can use [wireshark](https://www.wireshark.org) app for traffic analysis. With this utility you can view network packages and analyze their data.