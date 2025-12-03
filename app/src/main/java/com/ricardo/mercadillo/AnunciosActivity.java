package com.ricardo.mercadillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog; // ⬅️ Importación necesaria para el diálogo de eliminación
import android.content.Intent;    // ⬅️ Importación necesaria para iniciar la edición
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// Importamos la interfaz del adaptador
import com.ricardo.mercadillo.adapter.ProductoAdapter;
import com.ricardo.mercadillo.model.Producto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// ⬅️ CRÍTICO: La Activity ahora implementa la interfaz de acciones del adaptador
public class AnunciosActivity extends AppCompatActivity implements ProductoAdapter.OnAnuncioActionListener {

    private static final String TAG = "AnunciosActivity";

    // Vistas y Componentes
    private RecyclerView rvMisAnuncios;
    private TextView tvNoAnuncios;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference productosRef;

    private List<Producto> listaProductos;
    private ProductoAdapter productoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        // 1. Inicializar Vistas
        rvMisAnuncios = findViewById(R.id.rv_mis_productos);
        tvNoAnuncios = findViewById(R.id.tv_no_anuncios);

        // 2. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        productosRef = FirebaseDatabase.getInstance().getReference("productos");

        // 3. Configurar RecyclerView (MODIFICADO)
        listaProductos = new ArrayList<>();

        // ⬅️ CRÍTICO: Usamos el constructor MODIFICADO y pasamos 'this' como listener
        productoAdapter = new ProductoAdapter(this, listaProductos, this);

        // Configuración del LayoutManager
        rvMisAnuncios.setLayoutManager(new LinearLayoutManager(this));
        rvMisAnuncios.setAdapter(productoAdapter);

        // 4. Cargar los anuncios
        cargarMisAnuncios();

        //  Configurar el botón de retroceso
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ⬅️ NUEVO: Este método asegura que la lista se refresque al volver de EditarAnuncioActivity
    @Override
    protected void onResume() {
        super.onResume();
        cargarMisAnuncios();
    }

    private void cargarMisAnuncios() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 1. Verificar autenticación
        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado. No se pueden cargar anuncios.");
            mostrarMensajeNoAnuncios(true);
            return;
        }

        final String userId = currentUser.getUid();
        Log.d(TAG, "Iniciando carga de anuncios para el usuario ID: " + userId);

        // 2. CREAR LA CONSULTA FILTRADA
        Query queryMisAnuncios = productosRef.orderByChild("vendedorId").equalTo(userId);

        queryMisAnuncios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Producto> productosCargados = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot productoSnapshot : snapshot.getChildren()) {
                        Producto producto = productoSnapshot.getValue(Producto.class);
                        if (producto != null) {
                            // Asigna la clave del producto como su ID
                            producto.setId(productoSnapshot.getKey());
                            productosCargados.add(producto);
                        }
                    }
                }

                // 3. Actualizar UI
                if (productosCargados.isEmpty()) {
                    mostrarMensajeNoAnuncios(true);
                } else {
                    mostrarMensajeNoAnuncios(false);
                }

                productoAdapter.actualizarProductos(productosCargados);

                Log.d(TAG, "Anuncios cargados: " + productosCargados.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al leer anuncios: " + error.getMessage());
                Toast.makeText(AnunciosActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                mostrarMensajeNoAnuncios(true);
            }
        });
    }

    private void mostrarMensajeNoAnuncios(boolean mostrar) {
        if (mostrar) {
            tvNoAnuncios.setVisibility(View.VISIBLE);
            rvMisAnuncios.setVisibility(View.GONE);
        } else {
            tvNoAnuncios.setVisibility(View.GONE);
            rvMisAnuncios.setVisibility(View.VISIBLE);
        }
    }

    // -----------------------------------------------------------------
    //          IMPLEMENTACIÓN DE ProductoAdapter.OnAnuncioActionListener
    // -----------------------------------------------------------------

    // ⬅️ Lógica de Edición: Abre la nueva Activity
    @Override
    public void onEditarProducto(Producto producto) {
        Log.d(TAG, "Editando producto con ID: " + producto.getId());

        Intent intent = new Intent(AnunciosActivity.this, EditarAnuncioActivity.class);
        // Pasamos el ID del producto para que la Activity de edición sepa qué cargar
        intent.putExtra("PRODUCTO_ID", producto.getId());

        startActivity(intent);
        Toast.makeText(this, "Abriendo editor para: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
    }

    // ⬅️ Lógica de Eliminación: Muestra el diálogo de confirmación
    @Override
    public void onEliminarProducto(Producto producto) {
        Log.d(TAG, "Solicitud de eliminación para ID: " + producto.getId());
        mostrarDialogoConfirmacion(producto);
    }

    private void mostrarDialogoConfirmacion(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar el anuncio '" + producto.getNombre() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarProductoEnFirebase(producto.getId());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProductoEnFirebase(String productoId) {
        productosRef.child(productoId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AnunciosActivity.this, "✅ Anuncio eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    // La lista se actualiza automáticamente gracias al ValueEventListener en cargarMisAnuncios()
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar producto: " + e.getMessage());
                    Toast.makeText(AnunciosActivity.this, "❌ Error al eliminar el anuncio.", Toast.LENGTH_LONG).show();
                });
    }
}