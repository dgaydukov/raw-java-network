package com.network.raw.udp.unicast;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;

@Slf4j
public class UdpClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private int serverPort;

    private byte[] buffer;

    public UdpClient(int serverPort, int clientPort) {
        this.serverPort = serverPort;
        try{
            socket = new DatagramSocket(clientPort);
            address = InetAddress.getByName("localhost");
        } catch (SocketException | UnknownHostException ex){
            throw new RuntimeException(ex);
        }
    }

    public String send(String msg){
        try{
            buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, serverPort);
            socket.send(packet);
            buffer = new byte[255];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            log.info("Client received: address={}, port={}, msg={}", packet.getAddress(), packet.getPort(), received);
            return received;
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        int i = 0;
        while(true){
            send("msg_" + i++);
            sleep(5);
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