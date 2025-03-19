package com.network.raw.tcp.nio;

public class App {
    public static void main(String[] args) {
        int serverPort = 5555;
        String serverHost = "127.0.0.1";
        TcpChannelServer server = new TcpChannelServer(serverHost, serverPort);
        new Thread(server).start();
    }
}
