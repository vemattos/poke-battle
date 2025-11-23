package com.example.playerservice.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler simples que registra sessões por userId.
 * O cliente Unity envia o userId como primeira mensagem de texto para "se registrar".
 */
@Component
public class PlayerWebSocketHandler extends TextWebSocketHandler {

    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("[WS] Conexão aberta: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Esperamos que o payload seja apenas o userId para registrar a sessão:
        // ex: "36"
        try {
            String payload = message.getPayload();
            int userId = Integer.parseInt(payload.trim());
            sessions.put(userId, session);
            System.out.println("[WS] Player registrado: " + userId + " -> sessionId: " + session.getId());
            // opcional: enviar ack
            session.sendMessage(new TextMessage("{\"event\":\"REGISTERED\",\"userId\":" + userId + "}"));
        } catch (NumberFormatException ex) {
            System.out.println("[WS] Mensagem inesperada de " + session.getId() + ": " + message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // remove session das entradas (procura por value)
        sessions.entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
        System.out.println("[WS] Conexão fechada: " + session.getId());
    }

    public void sendToPlayer(int userId, String json) {
        try {
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(json));
                System.out.println("[WS] Enviado para " + userId + ": " + json);
            } else {
                System.out.println("[WS] Sessão não encontrada ou fechada para userId: " + userId);
            }
        } catch (Exception e) {
            System.out.println("[WS] Erro ao enviar WS: " + e.getMessage());
        }
    }
}
