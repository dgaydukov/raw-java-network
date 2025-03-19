package com.network.raw.udp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@Slf4j
public class ChannelUdpServer implements Runnable{
    private final ByteBuffer buffer = ByteBuffer.allocate(256);
    private final DatagramChannel server;

    public ChannelUdpServer(String serverHost, int serverPort){
        try{
            InetSocketAddress address = new InetSocketAddress(serverHost, serverPort);
            DatagramChannel channel = DatagramChannel.open();
            // bind null, cause it's not server
            server = channel.bind(address);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        while (true){
            receive();
        }
    }

    private void receive() {
        try{
            buffer.clear();
            SocketAddress address = server.receive(buffer);
            //String msg = extractMessage(buffer);
            String msg = new String(buffer.array(), 0, buffer.position());
            log.info("Server received: msg={}, address={}", msg, address);
            String responseMsg = "server response: originalMsg=" + msg;
            ByteBuffer response = ByteBuffer.wrap(responseMsg.getBytes());
            server.send(response, address);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    private String extractMessage(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }
}
