package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.ricardo.mercadillo.adapter.MensajeAdapter;
import com.ricardo.mercadillo.model.Mensaje;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String ID_RECEPTOR = "ID_RECEPTOR"; // Clave del Intent

    // Vistas de la interfaz
    private RecyclerView recyclerView;
    private EditText etMensaje;
    private ImageButton btnEnviarMensaje;
    private ImageButton btnEnviarImagen;
    private ImageButton btnBack; // Botón de retroceso
    private TextView tvNombreContacto;
    private TextView tvEstadoContacto; // Estado de actividad del contacto (opcional, avanzado)

    // Variables de Chat y Firebase
    private MensajeAdapter mensajeAdapter;
    private List<Mensaje> listaMensajes;
    private DatabaseReference chatRef;
    private String currentUserId;
    private String receptorId;
    private String chatRoomId;


    // private String receptorNombre = "DiegoDev";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Inicialización de Vistas y Datos de Usuario
        inicializarVistas();

        // Obtener IDs
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debe iniciar sesión para chatear.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = user.getUid();
        receptorId = getIntent().getStringExtra(ID_RECEPTOR);

        if (receptorId == null) {
            Toast.makeText(this, "Error: Receptor de chat no especificado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Determinar el ID de la Sala de Chat (Crucial)
        chatRoomId = generarChatRoomId(currentUserId, receptorId);
        chatRef = FirebaseDatabase.getInstance().getReference(Constantes.NODO_CHATS).child(chatRoomId);

        // 3. Configuración de la Cabecera
        configurarCabecera();

        // 🟢 NUEVO: Cargar el nombre del contacto al iniciar la actividad
        cargarNombreContacto();

        // 4. Configuración del RecyclerView
        configurarRecyclerView();

        // 5. Configuración del Listener de la Base de Datos
        escucharMensajes();

        // 6. Configuración de Listeners de UI
        configurarListeners();


        // tvNombreContacto.setText(receptorNombre);
        // tvEstadoContacto.setText("online");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // La lógica de carga del nombre ya está en onCreate()
    }

    // 🟢 NUEVO MÉTODO: Carga el nombre del contacto desde la tabla 'users'
    private void cargarNombreContacto() {
        // Consultamos el nodo 'users/receptorId'
        DatabaseReference contactoRef = FirebaseDatabase.getInstance().getReference("users").child(receptorId);

        contactoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Asumimos que el nombre del usuario se guarda bajo el campo 'nombre'
                String nombre = snapshot.child("nombre").getValue(String.class);

                if (nombre != null) {
                    tvNombreContacto.setText(nombre);
                } else {
                    // Si no se encuentra el nombre, mostramos un identificador por defecto
                    tvNombreContacto.setText("Usuario Desconocido");
                }
                tvEstadoContacto.setText("online"); // Estado asumido
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al cargar nombre de contacto: " + error.getMessage());
                tvNombreContacto.setText("Error al cargar");
            }
        });
    }

    // Método estático para generar un ID de sala único y consistente
    private String generarChatRoomId(String uid1, String uid2) {
        // Ordena los IDs para garantizar que el ID de la sala sea el mismo para ambos usuarios
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }

    private void inicializarVistas() {
        // Cabecera
        tvNombreContacto = findViewById(R.id.tvContactName);
        tvEstadoContacto = findViewById(R.id.tvContactStatus);
        btnBack = findViewById(R.id.btnBack);

        // Lista de mensajes
        recyclerView = findViewById(R.id.recyclerViewChat);

        // Área de entrada
        etMensaje = findViewById(R.id.editTextMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviar);
        btnEnviarImagen = findViewById(R.id.btnAttachImage);
    }

    private void configurarCabecera() {
        // Listener para el botón de retroceso (flecha <- )
        btnBack.setOnClickListener(v -> finish());
    }

    private void configurarRecyclerView() {
        // Inicializa la lista y el adaptador (vacío al inicio)
        listaMensajes = new ArrayList<>();
        // El ID del usuario actual es crucial para el adaptador
        mensajeAdapter = new MensajeAdapter(this, listaMensajes, currentUserId);

        // Configuramos el RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Hacemos que la lista inicie desde abajo (los mensajes más nuevos)
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mensajeAdapter);
    }

    private void configurarListeners() {
        // Listener para el botón de Enviar Mensaje (texto)
        btnEnviarMensaje.setOnClickListener(v -> enviarMensaje());

        // Listener para el botón de Enviar Imagen (simulación)
        btnEnviarImagen.setOnClickListener(v -> {
            Toast.makeText(this, "La función de envío de imágenes no está implementada.", Toast.LENGTH_SHORT).show();
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();

        if (!texto.isEmpty()) {
            // 1. Crear el nuevo nodo de mensaje
            DatabaseReference nuevoMensajeRef = chatRef.push();
            String mensajeId = nuevoMensajeRef.getKey();

            long timestamp = Constantes.obtenerTiempoDis();

            Mensaje nuevoMensaje = new Mensaje(
                    mensajeId,
                    currentUserId, // Remitente
                    texto,
                    timestamp,
                    Constantes.TIPO_MENSAJE_TEXTO
            );

            // 2. Guardar en la Base de Datos
            if (mensajeId != null) {
                nuevoMensajeRef.setValue(nuevoMensaje)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Mensaje enviado con éxito: " + mensajeId);
                            // Limpiar la caja de texto
                            etMensaje.setText("");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Fallo al enviar mensaje: ", e);
                        });
            }

        } else {
            Toast.makeText(this, "Escriba un mensaje.", Toast.LENGTH_SHORT).show();
        }
    }

    private void escucharMensajes() {
        // Usamos ChildEventListener para manejar adiciones, cambios y eliminaciones individuales.
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Mensaje mensaje = snapshot.getValue(Mensaje.class);
                if (mensaje != null) {
                    // Aseguramos que el ID del mensaje se establezca desde la clave de Firebase
                    mensaje.setId(snapshot.getKey());

                    // 1. Añadir el mensaje a la lista
                    listaMensajes.add(mensaje);

                    // 2. Ordenar la lista por timestamp (importante si los mensajes llegan fuera de orden)
                    Collections.sort(listaMensajes, Comparator.comparing(Mensaje::getTimestamp));

                    // 3. Notificar al adaptador
                    mensajeAdapter.notifyDataSetChanged();

                    // 4. Desplazar la lista al último mensaje
                    recyclerView.scrollToPosition(listaMensajes.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Lógica para manejar la edición o el cambio de estado de un mensaje (ej. mensaje editado)
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Lógica para eliminar el mensaje de la lista (ej. si fue borrado)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Los mensajes no se mueven si usamos timestamp para ordenar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al escuchar mensajes: " + error.getMessage(), error.toException());
                Toast.makeText(ChatActivity.this, "Fallo al cargar chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}