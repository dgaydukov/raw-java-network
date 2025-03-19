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
    private final InetSocketAddress serverAddress;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public ChannelUdpClient(String serverHost, int serverPort){
        try{
            serverAddress = new InetSocketAddress(serverHost, serverPort);
            DatagramChannel channel = DatagramChannel.open();
            // bind null, cause it's not server
            client = channel.bind(null);
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
            client.send(sendBuffer, serverAddress);
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
