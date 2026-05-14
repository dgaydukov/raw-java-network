package com.network.raw.tcp.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class TcpServer implements Runnable{
    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;

    public TcpServer(int serverPort) {
        try{
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    public void handleMessage(Socket client){
        while (true){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = in.readLine();
                log.info("Server received: msg={}, address={}, port={}", msg, client.getInetAddress(), client.getPort());
                if (msg == null){
                    log.info("Client disconnected: address={}, port={}", client.getInetAddress(), client.getPort());
                    break;
                }
                out.println("server response, originalMsg=" + msg);
            } catch (IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }

    public void handleUser(){
        try {
            final Socket client = serverSocket.accept();
            log.info("Client connected: address={}, port={}", client.getInetAddress(), client.getPort());
            new Thread(()-> handleMessage(client)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true){
            handleUser();
        }
    }
}
