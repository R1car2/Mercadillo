package com.ricardo.mercadillo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
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
                        Toast.makeText(this, "Imagen seleccionada correctamente.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Selección de imagen cancelada.",
                            Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout de publicación
        setContentView(R.layout.activity_publicar);
        // 1. Inicializar Vistas
        // Asegúrate de que activity_publicar.xml contenga estos IDs
        ivImagen = findViewById(R.id.iv_publicar_imagen);
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        etNombre = findViewById(R.id.et_publicar_nombre);
        etPrecio = findViewById(R.id.et_publicar_precio);
        etDescripcion = findViewById(R.id.et_publicar_descripcion);
        btnPublicar = findViewById(R.id.btn_publicar_producto);
        // 2. Manejar el clic para seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        // 3. Manejar el clic del botón Publicar (Simulación)
        //MODIFICACION3: Se llama a la nueva función de validación
        btnPublicar.setOnClickListener(v -> {
            if (validarCampos()) {
                simularPublicacion();
            }
        });
    }
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
    //MODIFICACION3: Se añade la función de validación
    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        // 1. Validar Imagen Seleccionada
        if (imagenUriSeleccionada == null) {
            Toast.makeText(this, "Debes seleccionar una imagen para el producto.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        // 2. Validar Nombre
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre del producto es obligatorio.");
            return false;
        }
        // 3. Validar Precio
        if (TextUtils.isEmpty(precio) || !precio.matches("^[0-9]+([,.][0-9]{1,2})?$")) {
            etPrecio.setError("Ingresa un precio válido (solo números y opcionalmente dos decimales).");
            return false;
        }
        // 4. Validar Descripción (mínimo, aunque no obligatorio, es buena práctica)
        if (TextUtils.isEmpty(descripcion) || descripcion.length() < 10) {
            etDescripcion.setError("La descripción es obligatoria y debe tener al menos 10 caracteres.");
            return false;
        }
        return true;
    }
    private void simularPublicacion() {
        String nombre = etNombre.getText().toString().trim();
        // Lógica de publicación real (Firebase) iría aquí.
        Toast.makeText(this, "Producto '" + nombre + "' listo para subir (Simulación OK).",
                Toast.LENGTH_LONG).show();
        // Volver a Home
        finish();
    }
}
