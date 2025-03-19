package com.network.raw.udp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@Slf4j
public class ChannelUdpClient implements Runnable{
    private final DatagramChannel client;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public ChannelUdpClient(String serverHost, int serverPort, String clientHost, int clientPort){
        try{
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            InetSocketAddress clientAddress = new InetSocketAddress(clientHost, clientPort);
            DatagramChannel channel = DatagramChannel.open();
            // bind can be null, cause it's not server, random port would be assigned
            client = channel.bind(clientAddress);
            client.connect(serverAddress);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        int i = 0;
        while (true){
            send("msg_" + i++);
            sleep(5);
        }
    }

    private void send(String msg){
        ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes());
        try {
            client.write(sendBuffer);
            SocketAddress address = client.receive(buffer);
            String received = new String(buffer.array(), 0, buffer.position());
            log.info("Client received: msg={}, address={}", received, address);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
