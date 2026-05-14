package com.network.raw.tcp.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

@Slf4j
public class TcpServer implements Runnable{
    private ServerSocket server;
    private PrintWriter out;
    private BufferedReader in;

    public TcpServer(int serverPort) {
        try{
            server = new ServerSocket(serverPort);
            log.info("Server started: address={}:{}", server.getInetAddress().getHostName(), server.getLocalPort());
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
                log.info("Server received: msg={}, address={}:{}", msg, client.getInetAddress().getHostName(), client.getPort());
                if (msg == null){
                    log.info("Client disconnected: address={}:{}", client.getInetAddress().getHostName(), client.getPort());
                    break;
                }
                out.println("server response, originalMsg=" + msg);
            } catch (SocketException ex){
                log.error("Client forcibly disconnected: address={}:{}", client.getInetAddress().getHostName(), client.getPort(), ex);
                break;
            } catch (IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }

    public void handleUser(){
        try {
            final Socket client = server.accept();
            log.info("Client connected: address={}:{}", client.getInetAddress().getHostName(), client.getPort());
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
