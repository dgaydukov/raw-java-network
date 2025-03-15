package com.network.raw.tcp.socket;

import lombok.SneakyThrows;
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

    public void handleMessage(Socket clientSocket){
        while (true){
            try{
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String msg = in.readLine();
                log.info("Server received: msg={}, address={}, port={}", msg, clientSocket.getInetAddress(), clientSocket.getPort());
                if (msg == null){
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
            final Socket clientSocket = serverSocket.accept();
            log.info("Client connected: address={}, port={}", clientSocket.getInetAddress(), clientSocket.getPort());
            new Thread(()->{
                    handleMessage(clientSocket);
            }).start();
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
