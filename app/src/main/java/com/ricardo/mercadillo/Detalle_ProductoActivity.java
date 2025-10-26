package com.ricardo.mercadillo; // ⬅️ Paquete actualizado

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ricardo.mercadillo.R; // ⬅️ Referencia al R de tu paquete
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class Detalle_ProductoActivity extends AppCompatActivity { // ⬅️ Nombre de clase actualizado

    public static final String EXTRA_PRODUCTO_ID = "producto_id";
    // SEMANA 4: Clave para recibir el número de teléfono del vendedor
    public static final String EXTRA_TELEFONO_VENDEDOR = "extra_telefono_vendedor";

    // Vistas
    private ImageView ivImagenDetalle;
    private TextView tvNombreDetalle;
    private TextView tvPrecioDetalle;
    private TextView tvDescripcionDetalle;
    private TextView tvVendedorDetalle;
    private ExtendedFloatingActionButton fabContactar;

    // SEMANA 4 Variable para almacenar el teléfono del vendedor
    private String telefonoVendedor;
    private static final String TAG = "Detalle_Producto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // 1. Inicializar Vistas
        ivImagenDetalle = findViewById(R.id.iv_detalle_imagen);
        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvVendedorDetalle = findViewById(R.id.tv_detalle_vendedor);
        fabContactar = findViewById(R.id.fab_contactar);

        // 2. Obtener datos del Intent
        Intent intent = getIntent();
        String productoId = intent.getStringExtra(EXTRA_PRODUCTO_ID);

        // SEMANA 4: Obtener el número de teléfono enviado por el Intent
        if (intent.hasExtra(EXTRA_TELEFONO_VENDEDOR)) {
            telefonoVendedor = intent.getStringExtra(EXTRA_TELEFONO_VENDEDOR);
        } else {
            // 💡 CORRECCIÓN: Asignamos el harcodeo aquí como valor de respaldo
            telefonoVendedor = "998110920";
            Log.w(TAG, "ADVERTENCIA: No se recibió el número de teléfono del vendedor. Usando número harcodeado: " + telefonoVendedor);
        }

        if (productoId != null) {
            Toast.makeText(this, "Detalles cargados para el ID: " + productoId, Toast.LENGTH_SHORT).show();
            cargarDatosSimulados(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.", Toast.LENGTH_LONG).show();
            finish();
        }

        // 3. Botón Contactar (Manejador de clic del FAB)
        // SEMANA 4: Ahora el botón 'Contactar' ejecuta la función de llamada
        fabContactar.setOnClickListener(v -> realizarLlamada());
    }

    /**
     * Datos de ejemplo para probar la interfaz (Simulación de DB).
     */
    private void cargarDatosSimulados(String id) {
        tvNombreDetalle.setText("Bicicleta Eléctrica Turbo X" + id.substring(0, 2));
        tvPrecioDetalle.setText("$450.000 CLP");
        tvDescripcionDetalle.setText("Modelo 2024, casi nueva. Perfecta para la ciudad y subir cuestas sin esfuerzo. Incluye cargador y garantía.");
        tvVendedorDetalle.setText("Usuario: DiegoDev");
    }

    /**
     * Prepara y ejecuta el Intent para iniciar una llamada telefónica.
     */
    private void realizarLlamada() {
        // Validación: Verifica si hay un número disponible
        // Si usamos el valor harcodeado, esta validación pasará.
        if (telefonoVendedor == null || telefonoVendedor.isEmpty()) {
            Toast.makeText(this, "El vendedor no ha proporcionado un número de contacto.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Construye el URI de la llamada. El prefijo "tel:" es crucial.
            Uri telefonoUri = Uri.parse("tel:" + telefonoVendedor);

            // Crea el Intent de acción DIAL (abre el marcador, no inicia la llamada directamente)
            Intent intentLlamada = new Intent(Intent.ACTION_DIAL, telefonoUri);

            // Verificación: Comprueba si el dispositivo tiene una aplicación capaz de manejar el Intent.
            if (intentLlamada.resolveActivity(getPackageManager()) != null) {
                startActivity(intentLlamada);
            } else {
                Toast.makeText(this, "No se encontró una aplicación para realizar llamadas en el dispositivo.", Toast.LENGTH_LONG).show();
            }
            // Manejo de Excepciones: Para errores inesperados al lanzar el Intent
        } catch (Exception e) {
            Log.e(TAG, "Error al intentar iniciar el Intent de llamada: " + e.getMessage());
            Toast.makeText(this, "No se pudo iniciar la función de llamada.", Toast.LENGTH_SHORT).show();
        }
    }
}