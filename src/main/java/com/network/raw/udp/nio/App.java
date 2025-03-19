package com.network.raw.udp.nio;

public class App {
    public static void main(String[] args) {
        final int serverPort = 5555;
        final int clientPort = 4444;
        final String serverHost = "127.0.0.1";
        ChannelUdpServer server = new ChannelUdpServer(serverHost, serverPort);
        ChannelUdpClient client = new ChannelUdpClient(serverHost, serverPort);
        new Thread(server).start();
        new Thread(client).start();
    }
}
