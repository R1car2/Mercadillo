package com.ricardo.mercadillo.model;

public class Mensaje {

    private String id;
    private String remitenteId;
    private String contenido;
    private long timestamp;
    private String tipo; // Campo agregado para diferenciar TEXTO, IMAGEN, etc.

    public Mensaje() {
        // Constructor vacío requerido por Firebase Realtime Database
    }

    // Constructor completo actualizado
    public Mensaje(String id, String remitenteId, String contenido, long timestamp, String tipo) {
        this.id = id;
        this.remitenteId = remitenteId;
        this.contenido = contenido;
        this.timestamp = timestamp;
        this.tipo = tipo;
    }

    // Getters y Setters (actualizados y nuevos)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Nuevo Getter y Setter para el tipo de mensaje
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}