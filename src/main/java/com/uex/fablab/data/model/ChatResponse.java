package com.uex.fablab.data.model;

/**
 * Modelo para respuesta de chat saliente.
 */
public class ChatResponse {
    private String response;

    public ChatResponse() {
    }

    public ChatResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
