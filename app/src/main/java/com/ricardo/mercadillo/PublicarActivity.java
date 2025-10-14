package com.ricardo.mercadillo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.ricardo.mercadillo.R;

public class PublicarActivity extends AppCompatActivity {

    // Vistas
    private ImageView ivImagen;
    private Button btnSeleccionarImagen;
    private TextInputEditText etNombre;
    private TextInputEditText etPrecio;
    private TextInputEditText etDescripcion;
    private Button btnPublicar;

    private Uri imagenUriSeleccionada;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Se ha seleccionado una imagen
                    imagenUriSeleccionada = result.getData().getData();
                    if (imagenUriSeleccionada != null) {
                        // Muestra la imagen en el ImageView
                        ivImagen.setImageURI(imagenUriSeleccionada);
                        Toast.makeText(this, "Imagen seleccionada correctamente.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Selección de imagen cancelada.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout de publicación
        setContentView(R.layout.activity_publicar);

        // 1. Inicializar Vistas
        ivImagen = findViewById(R.id.iv_publicar_imagen);
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        etNombre = findViewById(R.id.et_publicar_nombre);
        etPrecio = findViewById(R.id.et_publicar_precio);
        etDescripcion = findViewById(R.id.et_publicar_descripcion);
        btnPublicar = findViewById(R.id.btn_publicar_producto);

        // 2. Manejar el clic para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());

        // 3. Manejar el clic del botón Publicar (Simulación)
        btnPublicar.setOnClickListener(v -> simularPublicacion());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void simularPublicacion() {
        String nombre = etNombre.getText().toString().trim();

        if (imagenUriSeleccionada == null) {
            Toast.makeText(this, "Debes seleccionar una imagen para publicar.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El producto necesita un nombre.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí iría la lógica real para subir el producto (Firebase, backend, etc).
        Toast.makeText(this, "Producto '" + nombre + "' listo para subir (Simulación OK).", Toast.LENGTH_LONG).show();

        // Cerrar activity tras publicar
        finish();
    }
}
