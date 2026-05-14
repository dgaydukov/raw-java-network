package com.network.raw.tcp.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Random;

@Slf4j
public class TcpClient implements Runnable{
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    final int clientId;
    final long numOfMessages;

    public TcpClient(int serverPort, String serverHost, long numOfMessages){
        clientId = new Random().nextInt(100, 999);
        this.numOfMessages = numOfMessages;
        try{
            client = new Socket(serverHost, serverPort);
            log.info("Client started: address={}:{}", client.getInetAddress().getHostName(), client.getLocalPort());
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }


    public TcpClient(int serverPort, String serverHost){
        this(serverPort, serverHost, Long.MAX_VALUE);
    }

    public void sendAndReceive(String msg) {
        try{
            out.println(msg);
            String res = in.readLine();
            log.info("Client received: msg={}", res);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        for (long i = 0; i <= numOfMessages; i++) {
            String msg = "msg_" + clientId + "__" + i++;
            sendAndReceive(msg);
            sleep(1);
        }
        try {
            client.close();
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
