package com.network.raw.udp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

@Slf4j
public class UdpServer implements Runnable {
    private DatagramSocket socket;
    private byte[] buffer;

    public UdpServer(int serverPort) {
        try{
            socket = new DatagramSocket(serverPort);
            buffer = new byte[256];
        } catch (SocketException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String received = new String(packet.getData(), 0, packet.getLength());
                log.info("Server received: address={}, port={}, msg={}", address, port, received);
                String responseMsg = "server response: originalMsg=" + received;
                buffer = responseMsg.getBytes();
                DatagramPacket response = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(response);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
