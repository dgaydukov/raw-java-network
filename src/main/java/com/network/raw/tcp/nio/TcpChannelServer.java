package com.network.raw.tcp.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class TcpChannelServer implements Runnable{
    private static final String TCP_CLOSE = "TCP_CLOSE";
    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public TcpChannelServer(String serverHost, int serverPort){
        try{
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            serverSocket.bind(serverAddress);
            // by default it's blocking, se we need to set it to non-blocking
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        addClient(selector, serverSocket);
                    }
                    if (key.isReadable()) {
                        sendMessage(buffer, key);
                    }
                    iterator.remove();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addClient(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void sendMessage(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        int r = client.read(buffer);
        String receivedMsg = new String(buffer.array(), 0, buffer.position());
        // we trim, because when we send with netcat, it adds \n to the end of message, apparently because of enter button
        String msg = receivedMsg.trim();
        SocketAddress clientAddress = client.getRemoteAddress();
        log.info("Server received: r={}, msg={}, address={}", r, msg, clientAddress);
        if (r == -1 || TCP_CLOSE.equals(msg)) {
            client.close();
            log.info("closing connection: address={}", clientAddress);
        }
        else {
            String responseMsg = "server response, originalMsg=" + receivedMsg;
            ByteBuffer response = ByteBuffer.wrap(responseMsg.getBytes());
            client.write(response);
        }
    }
}
