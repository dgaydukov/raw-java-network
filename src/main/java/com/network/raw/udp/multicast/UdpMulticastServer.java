package com.network.raw.udp.multicast;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Slf4j
public class UdpMulticastServer implements Runnable {
    protected MulticastSocket socket;
    protected byte[] buf = new byte[256];

    public UdpMulticastServer(int serverPort, String multicastAddress ){
        try{
            socket = new MulticastSocket(serverPort);
            InetAddress group = InetAddress.getByName(multicastAddress);
            socket.joinGroup(group);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void run() {
        while (true) {
            try{
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                log.info("Server received: msg={}, address={}, port={}", received, packet.getAddress(), packet.getPort());
            } catch (IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }
}
