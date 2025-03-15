package com.network.raw.tcp.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class TcpClient implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public TcpClient(int serverPort, String serverHost){
        try{
            clientSocket = new Socket(serverHost, serverPort);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }

    }

    public void sendAndReceive(String msg) {
        try{
            System.out.println("sendAndReceive");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
            String msg = "msg_" + i++;
            sendAndReceive(msg);
            sleep(1);
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
