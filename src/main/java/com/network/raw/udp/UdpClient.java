package com.network.raw.udp;

import java.io.IOException;
import java.net.*;

public class UdpClient implements Runnable {
    private DatagramSocket socket;
    private InetAddress address;
    private int serverPort;

    private byte[] buffer;

    public UdpClient(int serverPort) {
        this.serverPort = serverPort;
        try{
            socket = new DatagramSocket();
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
            return received;
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        int i = 0;
        while(true){
            String msg = send("msg_" + i++);
            System.out.println("Client received: msg=" + msg);
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
