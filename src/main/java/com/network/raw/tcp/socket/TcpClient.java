package com.network.raw.tcp.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Random;

@Slf4j
public class TcpClient implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    final int clientId;

    public TcpClient(int serverPort, String serverHost){
        clientId = new Random().nextInt(100, 999);
        try{
            clientSocket = new Socket(serverHost, serverPort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }

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
        int i = 0;
        while (true) {
            String msg = "msg_" + clientId + "__" + i++;
            sendAndReceive(msg);
            sleep(30);
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
