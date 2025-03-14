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
If you launch [UDP App](/src/main/java/com/network/raw/udp/App.java) and start wireshark, you can see that client `localhost:4444` sending messages to server `localhost:5555` and getting UDP message back.


### TCP

### Wireshark
For better understanding of network communication you can use [wireshark](https://www.wireshark.org) app for traffic analysis. With this utility you can view network packages and analyze their data.