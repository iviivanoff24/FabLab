package com.uex.fablab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uex.fablab.data.model.ChatMessage;
import com.uex.fablab.data.model.ChatResponse;
import com.uex.fablab.data.services.ChatService;

/**
 * Controlador REST para el chat con el asistente virtual.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Envía un mensaje al asistente y obtiene respuesta.
     *
     * @param chatMessage mensaje del usuario
     * @return respuesta del asistente
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatMessage chatMessage) {
        ChatResponse response = chatService.processMessage(chatMessage);
        return ResponseEntity.ok(response);
    }
}
