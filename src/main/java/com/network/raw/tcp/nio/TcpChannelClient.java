package com.network.raw.tcp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class TcpChannelClient implements Runnable{
    private final InetSocketAddress serverAddress;
    private final SocketChannel client;

    public TcpChannelClient(String serverHost, int serverPort){
        try{
            client = SocketChannel.open();
            serverAddress = new InetSocketAddress(serverHost, serverPort);
            client.configureBlocking(false);
            client.connect(serverAddress);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {
            sendAndReceive("msg_" + i++);
        }
    }

    public void sendAndReceive(String msg){
        try{
            while (!client.finishConnect()) {
                log.warn("waiting to finish connection");
            }

            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            client.write(buffer);
            var receivedBuffer = ByteBuffer.allocate(256);
            // since it's non-blocking, we have to wait for server response
            sleep(100);
            client.read(receivedBuffer);
            String receivedMsg = new String(receivedBuffer.array(), 0, receivedBuffer.position());
            log.info("Client Received: msg={}", receivedMsg);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
        sleep(5_000);
    }

    public void sleep(long ms){
        try{
            Thread.sleep(ms);
        } catch (InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }
}
