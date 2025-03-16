package com.network.raw.tcp.ws;


import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@ServerEndpoint(value = "/chat")
@Slf4j
public class WsServer {
    private final Map<String, Session> users = new LinkedHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        String sessionId = session.getId();
        log.info("onOpen: sessionId={}", sessionId);
        users.put(sessionId, session);
    }

    @OnMessage
    public void onMessage(Session session, String msg) throws IOException {
        log.info("onMessage: sessionId={}, msg={}", session.getId(), msg);
        session.getBasicRemote().sendText("hello");
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.info("onClose: sessionId={}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        log.error("onError: sessionId={}", session.getId(), ex);
    }

    private void broadcast(String msg){
        users.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}