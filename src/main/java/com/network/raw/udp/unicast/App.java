package com.network.raw.udp.unicast;

public class App {
  public static void main(String[] args) {
    final int serverPort = 5555;
    final int clientPort = 4444;
    final String serverHost = "127.0.0.1";
    //UdpServer server = new UdpServer(serverPort);
    UdpClient client = new UdpClient(clientPort, serverPort, serverHost);
    //new Thread(server).start();
    new Thread(client).start();
    System.out.println("Run UDP server & client");
  }
}
