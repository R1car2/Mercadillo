package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ricardo.mercadillo.adapter.ConversacionAdapter;
import com.ricardo.mercadillo.model.Conversacion;
import com.ricardo.mercadillo.model.Mensaje;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // Necesario para el contador atómico

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    private RecyclerView rvConversaciones;
    private TextView tvNoChats;
    private ConversacionAdapter conversacionAdapter;
    private List<Conversacion> listaConversaciones;

    private String currentUserId;
    private DatabaseReference chatsRef;
    private DatabaseReference usersRef; // Referencia a la tabla de usuarios

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Inicializar vistas
        Toolbar toolbar = findViewById(R.id.toolbar_chat_list);
        setSupportActionBar(toolbar);

        rvConversaciones = findViewById(R.id.rv_conversaciones);
        tvNoChats = findViewById(R.id.tv_no_chats);

        // Obtener ID del usuario actual
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Sesión no activa.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase
        chatsRef = FirebaseDatabase.getInstance().getReference(Constantes.NODO_CHATS);
        usersRef = FirebaseDatabase.getInstance().getReference("users"); // Referencia a la tabla de usuarios

        // Configurar RecyclerView
        listaConversaciones = new ArrayList<>();
        conversacionAdapter = new ConversacionAdapter(this, listaConversaciones);

        rvConversaciones.setLayoutManager(new LinearLayoutManager(this));
        rvConversaciones.setAdapter(conversacionAdapter);

        // Iniciar la carga de conversaciones
        cargarConversaciones();
    }

    /**
     * Lógica principal para cargar, filtrar y mapear conversaciones de Firebase,
     * incluyendo la consulta del nombre del contacto desde la tabla 'users'.
     */
    // ChatListActivity.java

    private void cargarConversaciones() {
        if (currentUserId == null) {
            tvNoChats.setVisibility(View.VISIBLE);
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // 1. Contar y preparar las listas
                List<String> salasDeUsuario = new ArrayList<>();
                List<Conversacion> conversacionesPendientes = new ArrayList<>();

                // Iterar sobre las salas y determinar cuántas nos pertenecen
                for (DataSnapshot chatRoomSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatRoomSnapshot.getKey();
                    if (chatRoomId != null && chatRoomId.contains(currentUserId)) {
                        salasDeUsuario.add(chatRoomId);
                    }
                }

                final int totalChatRooms = salasDeUsuario.size();

                if (totalChatRooms == 0) {
                    finalizarCarga(new ArrayList<>());
                    return;
                }

                final AtomicInteger loadedCount = new AtomicInteger(0);

                // 2. Iterar solo sobre las salas que pertenecen al usuario
                for (String chatRoomId : salasDeUsuario) {

                    DataSnapshot chatRoomSnapshot = snapshot.child(chatRoomId);

                    String otroUserId = extraerOtroUserId(chatRoomId, currentUserId);

                    Conversacion conversacionBase = obtenerUltimaConversacion(
                            chatRoomId,
                            otroUserId,
                            chatRoomSnapshot
                    );

                    if (conversacionBase != null) {

                        // 3. Consultar la tabla "users" para obtener el nombre real del contacto (ASÍNCRONO)
                        usersRef.child(otroUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String nombre = userSnapshot.child("nombre").getValue(String.class);

                                if (nombre != null) {
                                    conversacionBase.setNombreContacto(nombre);
                                } else {
                                    conversacionBase.setNombreContacto("Usuario Desconocido");
                                }

                                conversacionesPendientes.add(conversacionBase);

                                // 4. Finalizar si todas las consultas de nombre han terminado
                                if (loadedCount.incrementAndGet() == totalChatRooms) {
                                    finalizarCarga(conversacionesPendientes);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Fallo al cargar nombre de usuario: " + otroUserId + ", Error: " + error.getMessage());

                                // A pesar del error, marcamos la carga como completada para este chat
                                conversacionesPendientes.add(conversacionBase);
                                if (loadedCount.incrementAndGet() == totalChatRooms) {
                                    finalizarCarga(conversacionesPendientes);
                                }
                            }
                        });

                    } else {
                        // Si no hay mensajes, solo incrementamos el contador de carga
                        if (loadedCount.incrementAndGet() == totalChatRooms) {
                            finalizarCarga(conversacionesPendientes);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar conversaciones: " + error.getMessage());
                Toast.makeText(ChatListActivity.this, "Fallo al cargar la bandeja de entrada.", Toast.LENGTH_LONG).show();
                mostrarMensajeNoConversaciones(true);
            }
        });
    }

    /**
     * Método auxiliar llamado solo cuando todas las consultas asíncronas han terminado.
     */
    private void finalizarCarga(List<Conversacion> listaPendiente) {
        listaConversaciones.clear();

        // Solo añadimos las conversaciones que realmente tienen un último mensaje válido
        for (Conversacion conv : listaPendiente) {
            if (conv.getUltimoMensaje() != null && !conv.getUltimoMensaje().isEmpty()) {
                listaConversaciones.add(conv);
            }
        }

        // Ordenar la lista por el timestamp del último mensaje (más reciente primero)
        listaConversaciones.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));

        // 5. Actualizar la UI
        if (listaConversaciones.isEmpty()) {
            mostrarMensajeNoConversaciones(true);
        } else {
            conversacionAdapter.notifyDataSetChanged();
            mostrarMensajeNoConversaciones(false);
        }
    }


    /**
     * Auxiliar: Extrae el ID del usuario que no es el usuario actual del chatRoomId.
     */
    private String extraerOtroUserId(String chatRoomId, String currentUserId) {
        // El ID de la sala está en formato "UID_MENOR_UID_MAYOR"
        String[] uids = chatRoomId.split("_");
        if (uids.length == 2) {
            if (uids[0].equals(currentUserId)) {
                return uids[1];
            } else {
                return uids[0];
            }
        }
        return null;
    }


    /**
     * Auxiliar: Itera los mensajes de una sala de chat para obtener el último y el timestamp.
     */
    private Conversacion obtenerUltimaConversacion(
            String chatRoomId,
            String otroUserId,
            DataSnapshot chatRoomSnapshot
    ) {
        Mensaje ultimoMensaje = null;
        long ultimoTimestamp = 0;

        // Itera sobre los hijos y el último es el más reciente
        for (DataSnapshot mensajeSnapshot : chatRoomSnapshot.getChildren()) {
            Mensaje mensaje = mensajeSnapshot.getValue(Mensaje.class);
            if (mensaje != null) {
                ultimoMensaje = mensaje;
                ultimoTimestamp = mensaje.getTimestamp();
            }
        }

        if (ultimoMensaje != null) {
            // Inicializa el nombre temporalmente con el ID (será reemplazado asíncronamente)
            String nombreContactoTemporal = otroUserId;

            // Si el último mensaje es del tipo imagen, mostramos una etiqueta
            String contenido = ultimoMensaje.getContenido();
            if (ultimoMensaje.getTipo() != null && ultimoMensaje.getTipo().equals(Constantes.TIPO_MENSAJE_IMAGEN)) {
                contenido = "📸 Imagen";
            }

            return new Conversacion(
                    chatRoomId,
                    otroUserId,
                    nombreContactoTemporal, // Nombre temporal (será reemplazado)
                    contenido,
                    ultimoTimestamp
            );
        }
        return null;
    }

    /**
     * Auxiliar: Muestra/Oculta el mensaje de "No hay chats".
     */
    private void mostrarMensajeNoConversaciones(boolean mostrar) {
        if (mostrar) {
            tvNoChats.setVisibility(View.VISIBLE);
            rvConversaciones.setVisibility(View.GONE);
        } else {
            tvNoChats.setVisibility(View.GONE);
            rvConversaciones.setVisibility(View.VISIBLE);
        }
    }
}