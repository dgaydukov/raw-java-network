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
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public TcpServer(int serverPort) {
        try{
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public void handleRequest(){
        try{
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String msg = in.readLine();
            log.info("Server received: msg={}", msg);
            out.println("server response, originalMsg=" + msg);
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        while (true){
            handleRequest();
        }
    }
}
