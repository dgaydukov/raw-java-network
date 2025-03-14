package com.network.raw.udp.unicast;

public class App {
  public static void main(String[] args) {
    final int serverPort = 5555;
    final int clientPort = 4444;
    UdpServer server = new UdpServer(serverPort);
    UdpClient client = new UdpClient(serverPort, clientPort);
    new Thread(server).start();
    new Thread(client).start();
    System.out.println("Run UDP server & client");
  }
}
