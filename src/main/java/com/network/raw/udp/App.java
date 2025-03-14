package com.network.raw.udp;

public class App {
  public static void main(String[] args) {
    UdpServer server = new UdpServer(5555);
    UdpClient client = new UdpClient(5555);
    new Thread(server).start();
    new Thread(client).start();
    System.out.println("Run UDP server & client");
  }
}
