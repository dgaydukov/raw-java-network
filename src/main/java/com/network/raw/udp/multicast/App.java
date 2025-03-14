package com.network.raw.udp.multicast;


public class App {
  public static void main(String[] args) {
    final int serverPort = 5555;
    final int clientPort = 4444;
    final String multicastAddress = "230.0.0.0";
    UdpMulticastClient publisher = new UdpMulticastClient(clientPort, serverPort, multicastAddress);
    UdpMulticastServer receiver = new UdpMulticastServer(serverPort, multicastAddress);
    new Thread(publisher).start();
    new Thread(receiver).start();
    System.out.println("Run UDP Multicast");
  }
}
