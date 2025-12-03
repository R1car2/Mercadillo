package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth; // Necesario para obtener el UID del usuario actual

import com.ricardo.mercadillo.model.Producto;
import com.ricardo.mercadillo.adapter.CarruselImagenAdapter;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Detalle_ProductoActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCTO_ID = "producto_id";
    public static final String EXTRA_TELEFONO_VENDEDOR = "extra_telefono_vendedor"; // Mantengo la constante para consistencia

    // Vistas del Producto
    // private ImageView ivImagenDetalle; // COMENTADA/ELIMINADA: Reemplazada por ViewPager2
    private ViewPager2 viewPagerCarrusel;
    // private CircleIndicator3 indicator; // COMENTADA/ELIMINADA: Si lo necesitas, descomenta esto

    private TextView tvNombreDetalle;
    private TextView tvPrecioDetalle;
    private TextView tvDescripcionDetalle;
    private TextView tvMarcaDetalle;
    private TextView tvCategoriaDetalle;
    private TextView tvCondicionDetalle;
    private TextView tvDireccionDetalle;

    // Botones (Actualizado)
    private MaterialButton btnIniciarChat;
    private MaterialButton btnLlamarVendedor; // Renombrado de fabContactar

    // VISTAS DEL VENDEDOR
    private TextView tvVendedorNombre;
    private TextView tvVendedorEmail;
    private TextView tvVendedorTelefono;

    // Variables de Firebase
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance(); // Instancia de Auth
    private DatabaseReference productosRef;
    private DatabaseReference usuariosRef;

    // Variables de clase para la lógica de contacto
    private String telefonoVendedor = null;
    private String vendedorIdGlobal = null; // Necesario para el chat
    private static final String TAG = "Detalle_Producto";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        // Configuración del Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Producto");
        }

        // Inicialización de Firebase
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        usuariosRef = FirebaseDatabase.getInstance().getReference("users");

        // 1. Inicializar Vistas del Producto
        // ivImagenDetalle = findViewById(R.id.iv_detalle_imagen); // OBSOLETO
        viewPagerCarrusel = findViewById(R.id.viewPager_detalle_carrusel); // NUEVO CARRUSEL
        // indicator = findViewById(R.id.indicator_detalle); // Si lo usas, inicialízalo aquí

        tvNombreDetalle = findViewById(R.id.tv_detalle_titulo);
        tvPrecioDetalle = findViewById(R.id.tv_detalle_precio);
        tvDescripcionDetalle = findViewById(R.id.tv_detalle_descripcion);
        tvMarcaDetalle = findViewById(R.id.tv_detalle_marca);
        tvCategoriaDetalle = findViewById(R.id.tv_detalle_categoria);
        tvCondicionDetalle = findViewById(R.id.tv_detalle_condicion);
        tvDireccionDetalle = findViewById(R.id.tv_detalle_direccion);

        // 2. Inicializar Vistas del Vendedor
        tvVendedorNombre = findViewById(R.id.tv_detalle_vendedor);
        tvVendedorEmail = findViewById(R.id.tv_vendedor_email);
        tvVendedorTelefono = findViewById(R.id.tv_vendedor_telefono);

        // Inicialización de los botones (ACTUALIZADO)
        btnIniciarChat = findViewById(R.id.btn_iniciar_chat);
        btnLlamarVendedor = findViewById(R.id.btn_llamar_vendedor);

        // Placeholder mientras carga
        tvVendedorNombre.setText("Nombre: Cargando detalles...");

        // 3. Obtener datos del Intent
        Intent intent = getIntent();
        String productoId = intent.getStringExtra(EXTRA_PRODUCTO_ID);

        if (productoId != null) {
            Log.d(TAG, "Cargando detalles para el ID: " + productoId);
            cargarDatosProducto(productoId);
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del producto.", Toast.LENGTH_LONG).show();
            finish();
        }

        // 4. Configurar Listeners de Contacto (ACTUALIZADO)
        btnLlamarVendedor.setOnClickListener(v -> realizarLlamada());
        btnIniciarChat.setOnClickListener(v -> iniciarChat());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarDatosProducto(String productoId) {
        productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Producto producto = snapshot.getValue(Producto.class);

                        if (producto != null) {
                            mostrarDetallesProducto(producto);

                            // Capturamos el ID del vendedor (CRUCIAL)
                            String vendedorId = producto.getVendedorId();
                            vendedorIdGlobal = vendedorId;

                            if (vendedorId != null) {
                                cargarDatosVendedor(vendedorId);
                            } else {
                                Log.e(TAG, "Producto sin VendedorId.");
                                mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
                            }

                        } else {
                            mostrarDetallesProductoIndividual(snapshot);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al mapear el objeto Producto. Intentando lectura individual: " + e.getMessage(), e);
                        mostrarDetallesProductoIndividual(snapshot);
                    }
                } else {
                    Toast.makeText(Detalle_ProductoActivity.this, "Producto no encontrado en la base de datos.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al leer el producto de DB: " + error.getMessage(), error.toException());
                Toast.makeText(Detalle_ProductoActivity.this, "Error de Firebase: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDetallesProductoIndividual(@NonNull DataSnapshot snapshot) {
        // Lógica de lectura individual (si el mapeo falla)
        String nombre = snapshot.child("nombre").getValue(String.class);
        Double precioDouble = snapshot.child("precio").getValue(Double.class);
        String descripcion = snapshot.child("descripcion").getValue(String.class);
        String marca = snapshot.child("marca").getValue(String.class);
        String condicion = snapshot.child("condicion").getValue(String.class);
        String categoria = snapshot.child("categoria").getValue(String.class);
        String direccion = snapshot.child("direccion").getValue(String.class);
        String vendedorId = snapshot.child("vendedorId").getValue(String.class);

        // ... Asignación de texto y formateo (mantenido) ...
        tvNombreDetalle.setText(nombre != null ? nombre : "N/A");
        tvDescripcionDetalle.setText(descripcion != null ? descripcion : "Sin descripción");

        if (precioDouble != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
            String precioFormateado = currencyFormat.format(precioDouble);
            tvPrecioDetalle.setText(precioFormateado);
        } else {
            tvPrecioDetalle.setText("Precio: N/A");
        }

        tvMarcaDetalle.setText("Marca: " + (marca != null ? marca : "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (condicion != null ? condicion : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (categoria != null ? categoria : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (direccion != null ? direccion : "No provista"));

        // Lógica del carrusel para el mapeo individual:
        DataSnapshot imageUrlsSnapshot = snapshot.child("imageUrls");
        List<String> imageUrls = new ArrayList<>();
        if (imageUrlsSnapshot.exists()) {
            for (DataSnapshot urlSnapshot : imageUrlsSnapshot.getChildren()) {
                String url = urlSnapshot.getValue(String.class);
                if (url != null) {
                    imageUrls.add(url);
                }
            }
        }

        cargarCarrusel(imageUrls);

        // LECTURA 2: Buscar datos del vendedor
        vendedorIdGlobal = vendedorId; // CRUCIAL: Capturamos el ID
        if (vendedorId != null) {
            cargarDatosVendedor(vendedorId);
        } else {
            Log.e(TAG, "Producto sin VendedorId.");
            mostrarDetallesVendedor("Vendedor Desconocido", "N/A", null);
        }
    }

    private void cargarDatosVendedor(String vendedorId) {
        usuariosRef.child(vendedorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombre = snapshot.child("nombre").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String codigo = snapshot.child("codigoTelefono").getValue(String.class);
                String numero = String.valueOf(snapshot.child("telefono").getValue());

                if (nombre != null || email != null || codigo != null || numero != null) {
                    String telefonoCompleto = combinarTelefono(codigo, numero);

                    // PASO CLAVE: Guardar el teléfono completo en la variable de clase para la función de Llamar
                    telefonoVendedor = telefonoCompleto;

                    mostrarDetallesVendedor(nombre, email, telefonoCompleto);
                } else {
                    Log.w(TAG, "Usuario no encontrado o datos nulos para ID: " + vendedorId);
                    mostrarDetallesVendedor("Usuario No Encontrado", "No disponible", null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Fallo al cargar datos del vendedor: " + error.getMessage());
                mostrarDetallesVendedor("Error de Carga", "No disponible", null);
            }
        });
    }


    private void mostrarDetallesProducto(Producto producto) {
        // 1. Cargar el Carrusel de imágenes
        cargarCarrusel(producto.getImageUrls());

        // 2. Asignar Texto a las Vistas Principales (Título, Precio, Descripción)
        tvNombreDetalle.setText(producto.getNombre());
        tvDescripcionDetalle.setText(producto.getDescripcion());

        // Formatear precio
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        String precioFormateado = currencyFormat.format(producto.getPrecio());
        tvPrecioDetalle.setText(precioFormateado);

        // 3. Asignar Texto a las Vistas de ESPECIFICACIONES (Marca, Condición, Categoría, Dirección)
        tvMarcaDetalle.setText("Marca: " + (producto.getMarca() != null ? producto.getMarca() : "No especificada"));
        tvCondicionDetalle.setText("Condición: " + (producto.getCondicion() != null ? producto.getCondicion() : "N/A"));
        tvCategoriaDetalle.setText("Categoría: " + (producto.getCategoria() != null ? producto.getCategoria() : "N/A"));
        tvDireccionDetalle.setText("Retiro en: " + (producto.getDireccion() != null ? producto.getDireccion() : "No provista"));
    }


    private void cargarCarrusel(List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            CarruselImagenAdapter carruselAdapter = new CarruselImagenAdapter(this, imageUrls);
            viewPagerCarrusel.setAdapter(carruselAdapter);
            // Si usas indicadores (CircleIndicator3), descomenta la siguiente línea:
            // if (indicator != null) indicator.setViewPager(viewPagerCarrusel);
        } else {
            // Si la lista está vacía, mostramos un solo placeholder.
            List<String> defaultUrl = new ArrayList<>();
            defaultUrl.add(null);
            CarruselImagenAdapter carruselAdapter = new CarruselImagenAdapter(this, defaultUrl);
            viewPagerCarrusel.setAdapter(carruselAdapter);
        }
    }



    private void mostrarDetallesVendedor(String nombre, String email, String telefono) {
        // Nombre
        String displayNombre = (nombre != null && !nombre.isEmpty()) ? nombre : "Usuario Desconocido";
        tvVendedorNombre.setText("Nombre: " + displayNombre);

        // Email
        String displayEmail = (email != null && !email.isEmpty()) ? email : "No proporcionado";
        tvVendedorEmail.setText("Correo: " + displayEmail);

        // Teléfono
        String displayTelefono = (telefono != null && !telefono.isEmpty()) ? telefono : "No disponible";
        tvVendedorTelefono.setText("Teléfono: " + displayTelefono);

        // Habilitar/Deshabilitar el botón de llamar (mantenido)
        boolean telefonoValido = telefono != null && !telefono.isEmpty() && telefono.matches("^\\+?[0-9\\s()-]*$");
        btnLlamarVendedor.setEnabled(telefonoValido);

        // Habilitar/Deshabilitar el botón de chat (lógica simple)
        boolean chatValido = vendedorIdGlobal != null && mAuth.getUid() != null && !mAuth.getUid().equals(vendedorIdGlobal);
        btnIniciarChat.setEnabled(chatValido);
    }

    // --- Funciones de Contacto ---
    private void realizarLlamada() {
        if (telefonoVendedor == null || telefonoVendedor.isEmpty()) {
            Toast.makeText(this, "El vendedor no ha proporcionado un número de contacto para llamar.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri telefonoUri = Uri.parse("tel:" + telefonoVendedor);
            Intent intentLlamada = new Intent(Intent.ACTION_DIAL, telefonoUri);

            if (intentLlamada.resolveActivity(getPackageManager()) != null) {
                startActivity(intentLlamada);
            } else {
                Toast.makeText(this, "No se encontró una aplicación para realizar llamadas.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al intentar iniciar el Intent de llamada: " + e.getMessage());
            Toast.makeText(this, "No se pudo iniciar la función de llamada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarChat() {
        String currentUserId = mAuth.getUid();

        if (currentUserId == null) {
            Toast.makeText(this, "Debe iniciar sesión para iniciar un chat.", Toast.LENGTH_LONG).show();
            return;
        }

        if (vendedorIdGlobal == null || vendedorIdGlobal.isEmpty()) {
            Toast.makeText(this, "Error: ID del vendedor no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId.equals(vendedorIdGlobal)) {
            Toast.makeText(this, "No puedes iniciar un chat contigo mismo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navegar a ChatActivity (asumiendo que existe una clase ChatActivity)
        Intent intentChat = new Intent(Detalle_ProductoActivity.this, ChatActivity.class);
        intentChat.putExtra("ID_RECEPTOR", vendedorIdGlobal);
        // También puedes pasar el ID del producto para el contexto del chat
        intentChat.putExtra(EXTRA_PRODUCTO_ID, getIntent().getStringExtra(EXTRA_PRODUCTO_ID));
        startActivity(intentChat);
    }


    private String combinarTelefono(String codigo, String numero) {
        if (codigo != null && !codigo.isEmpty() && numero != null && !numero.isEmpty()) {
            String cleanedCodigo = codigo.replaceAll("[^0-9]", "");
            String cleanedNumero = numero.replaceAll("[^0-9]", "");
            return "+" + cleanedCodigo + cleanedNumero;
        }
        return null;
    }
}