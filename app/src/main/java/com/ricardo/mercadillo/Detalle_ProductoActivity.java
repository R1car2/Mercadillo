package com.ricardo.mercadillo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ricardo.mercadillo.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class Detalle_ProductoActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCTO_ID = "producto_id";

    // Claves para extras del Intent
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_PRECIO = "precio";
    public static final String EXTRA_DESCRIPCION = "descripcion";
    public static final String EXTRA_VENDEDOR = "vendedor";
    public static final String EXTRA_IMAGEN_RES_ID = "imagen_res_id";

    // Vistas
    private ImageView ivImagenDetalle;
    private TextView tvNombreDetalle;
    private TextView tvPrecioDetalle;
    private TextView tvDescripcionDetalle;
    private TextView tvVendedorDetalle;
    private ExtendedFloatingActionButton fabContactar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Setup Toolbar con botón de retroceso
        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inicializar vistas
        ivImagenDetalle = findViewById(R.id.iv_detalle_imagen);
        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvVendedorDetalle = findViewById(R.id.tv_detalle_vendedor);
        fabContactar = findViewById(R.id.fab_contactar);

        // Obtener datos del Intent
        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        String precio = getIntent().getStringExtra(EXTRA_PRECIO);
        String descripcion = getIntent().getStringExtra(EXTRA_DESCRIPCION);
        String vendedor = getIntent().getStringExtra(EXTRA_VENDEDOR);
        int imagenResId = getIntent().getIntExtra(EXTRA_IMAGEN_RES_ID, R.drawable.bicicleta);

        if (titulo == null || precio == null || descripcion == null || vendedor == null) {
            Toast.makeText(this, "Error: Faltan datos del producto.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Mostrar datos
        tvNombreDetalle.setText(titulo);
        tvPrecioDetalle.setText(precio);
        tvDescripcionDetalle.setText(descripcion);
        tvVendedorDetalle.setText(vendedor);
        ivImagenDetalle.setImageResource(imagenResId);

        // Botón Contactar
        fabContactar.setOnClickListener(v -> {
            Toast.makeText(this, "Simulando chat con el vendedor...", Toast.LENGTH_SHORT).show();
        });
    }
}
