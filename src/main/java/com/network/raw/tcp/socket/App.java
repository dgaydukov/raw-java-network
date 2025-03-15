package com.network.raw.tcp.socket;

public class App {
    public static void main(String[] args) {
        int serverPort = 5555;
        String serverHost = "127.0.0.1";
        TcpServer server = new TcpServer(serverPort);
        TcpClient client = new TcpClient(serverPort, serverHost);
        new Thread(server).start();
        new Thread(client).start();
        //new Thread(new TcpClient(serverPort, serverHost)).start();
    }
}
