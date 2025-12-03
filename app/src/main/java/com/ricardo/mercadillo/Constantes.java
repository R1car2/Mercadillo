package com.ricardo.mercadillo;

// Clase que contiene todas las constantes y métodos estáticos de utilidad
public class Constantes {

    // Constante para indicar el estado de un anuncio disponible
    public static final String anuncio_disponible = "Disponible";

    // --- CHAT Y MENSAJERÍA EN TIEMPO REAL ---
    public static final String NODO_CHATS = "chats";
    public static final String TIPO_MENSAJE_TEXTO = "texto";
    public static final String TIPO_MENSAJE_IMAGEN = "imagen";


    // --- LISTA DE CATEGORÍAS ---
    public static final String[] categorias = {
            "Todos",
            "Móbiles",
            "Ordenadores/Laptops",
            "Electrónica y electrodomésticos",
            "Vehículos",
            "Consolas y videojuegos",
            "Hogar y muebles",
            "Belleza y cuidado personal",
            "Libros",
            "Deportes",
            "Juguetes y figuras",
            "Mascotas"
    };

    // --- LISTA DE CONDICIONES ---
    public static final String[] condiciones = {
            "Nuevo",
            "Usado",
            "Renovado"
    };

    // Método para obtener el tiempo actual en milisegundos como Long
    public static long obtenerTiempoDis() {
        return System.currentTimeMillis();
    }
}