package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ricardo.mercadillo.model.Producto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class EditarAnuncioActivity extends AppCompatActivity {

    private static final String TAG = "EditarAnuncioActivity";

    // Componentes de la UI (Ajusta los IDs según tu layout)
    private EditText etNombre, etPrecio, etDescripcion;
    private Button btnGuardarCambios;

    // Firebase
    private DatabaseReference productoRef;
    private String productoId;
    private Producto productoActual; // Objeto para almacenar los datos actuales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ASUMIMOS que tienes un layout llamado activity_editar_anuncio.xml
        setContentView(R.layout.activity_editar_anuncio);

        // 1. Inicializar Vistas
        // Asegúrate de que estos IDs coincidan con tu layout de edición
        etNombre = findViewById(R.id.et_edit_nombre);
        etPrecio = findViewById(R.id.et_edit_precio);
        etDescripcion = findViewById(R.id.et_edit_descripcion);
        btnGuardarCambios = findViewById(R.id.btn_guardar_cambios);

        // Asumiendo un botón de retroceso en la toolbar:
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());


        // 2. Obtener el ID del Intent
        productoId = getIntent().getStringExtra("PRODUCTO_ID");
        if (productoId == null) {
            Toast.makeText(this, "Error: No se encontró el ID del producto.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Inicializar Referencia de Firebase para el producto específico
        productoRef = FirebaseDatabase.getInstance().getReference("productos").child(productoId);

        // 4. Cargar los datos actuales del producto
        cargarDatosProducto();

        // 5. Configurar Listener del botón Guardar
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    // --- Lógica de Carga ---

    private void cargarDatosProducto() {
        productoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productoActual = snapshot.getValue(Producto.class);
                if (productoActual != null) {
                    // Rellenar los campos de la UI con los datos actuales
                    etNombre.setText(productoActual.getNombre());
                    // Convertir double a String para EditText
                    etPrecio.setText(String.valueOf(productoActual.getPrecio()));
                    etDescripcion.setText(productoActual.getDescripcion());

                    // Aquí deberías cargar los demás campos (marca, categoría, etc.)
                    // usando Spinners o Textos según tu diseño.

                    Log.d(TAG, "Datos cargados para la edición.");
                } else {
                    Toast.makeText(EditarAnuncioActivity.this, "Producto no encontrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al cargar datos: " + error.getMessage());
                Toast.makeText(EditarAnuncioActivity.this, "Error al cargar datos.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    // --- Lógica de Guardado ---

    private void guardarCambios() {
        String nuevoNombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String nuevaDescripcion = etDescripcion.getText().toString().trim();

        if (nuevoNombre.isEmpty() || precioStr.isEmpty() || nuevaDescripcion.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        double nuevoPrecio;
        try {
            nuevoPrecio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio no es un número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un Map con solo los campos que se van a actualizar
        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("nombre", nuevoNombre);
        actualizaciones.put("precio", nuevoPrecio);
        actualizaciones.put("descripcion", nuevaDescripcion);

        // Añadir otros campos (ubicación, condición, etc.) si también se modificaron
        // actualizaciones.put("ubicacion", nuevoValorUbicacion);

        // Actualizar en Firebase
        productoRef.updateChildren(actualizaciones)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditarAnuncioActivity.this, "Anuncio actualizado con éxito.", Toast.LENGTH_SHORT).show();
                    // Regresar a AnunciosActivity. El onResume de AnunciosActivity recargará la lista.
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar: " + e.getMessage());
                    Toast.makeText(EditarAnuncioActivity.this, "Error al guardar cambios.", Toast.LENGTH_LONG).show();
                });
    }
}