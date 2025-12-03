package com.ricardo.mercadillo.model;

public class Conversacion {

    private String chatRoomId;
    private String otroUserId;
    private String nombreContacto;
    private String ultimoMensaje;
    private long timestamp;

    public Conversacion() {

    }

    public Conversacion(String chatRoomId, String otroUserId, String nombreContacto, String ultimoMensaje, long timestamp) {
        this.chatRoomId = chatRoomId;
        this.otroUserId = otroUserId;
        this.nombreContacto = nombreContacto;
        this.ultimoMensaje = ultimoMensaje;
        this.timestamp = timestamp;
    }

    // Getters y Setters

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getOtroUserId() {
        return otroUserId;
    }

    public void setOtroUserId(String otroUserId) {
        this.otroUserId = otroUserId;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}