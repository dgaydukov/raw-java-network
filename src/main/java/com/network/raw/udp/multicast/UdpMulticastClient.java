package com.network.raw.udp.multicast;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class UdpMulticastClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buffer = new byte[256];
    private int serverPort;

    public UdpMulticastClient(int clientPort, int serverPort, String multicastAddress) {
        this.serverPort = serverPort;
        try{
            socket = new DatagramSocket(clientPort);
            address = InetAddress.getByName(multicastAddress);
        } catch (SocketException | UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public void run() {
        int i = 0;
        while (true){
            try {
                String msg = "msg_" + i++;
                buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, serverPort);
                log.info("client sending: msg={}, address={}, port={}", msg, address, serverPort);
                socket.send(packet);
                sleep(5);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void sleep(long sec){
        try{
            Thread.sleep(sec * 1000);
        } catch (InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }
}
