package com.ricardo.mercadillo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

// Importación de Glide para la carga de imágenes
import com.bumptech.glide.Glide;

import com.ricardo.mercadillo.Detalle_ProductoActivity;
import com.ricardo.mercadillo.R;
import com.ricardo.mercadillo.model.Producto;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adaptador modificado para soportar el filtrado de productos y gestión de anuncios
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private static final String TAG = "ProductoAdapter";

    private final Context context;
    private List<Producto> listaProductos;
    private final List<Producto> listaOriginal;

    private final OnAnuncioActionListener listener;

    public interface OnAnuncioActionListener {
        void onEditarProducto(Producto producto);
        void onEliminarProducto(Producto producto);
    }

    public ProductoAdapter(Context context, List<Producto> lista, OnAnuncioActionListener listener) {
        this.context = context;
        this.listaProductos = new ArrayList<>(lista);
        this.listaOriginal = new ArrayList<>(lista);
        this.listener = listener; // Inicialización del listener
    }

    // NOTA: El constructor antiguo (sin listener) debe ser eliminado o marcado como obsoleto.

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_producto, parent, false);
        return new ProductoViewHolder(view);
    }


    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        // A. Asignar datos del Producto a las Vistas
        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));
        holder.ivImagenProducto.setImageResource(R.drawable.agregar_img);

        if (producto.getImageUrls() != null && !producto.getImageUrls().isEmpty()) {
            String imageUrl = producto.getImageUrls().get(0);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.agregar_img)
                        .error(R.drawable.agregar_img)
                        .centerCrop()
                        .into(holder.ivImagenProducto);
            }
        }


        // B. Manejo del Clic en el Ítem (Ir al Detalle)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Detalle_ProductoActivity.class);
            intent.putExtra(Detalle_ProductoActivity.EXTRA_PRODUCTO_ID, producto.getId());
            context.startActivity(intent);
        });


        if (listener != null) {

            // Botón Editar
            holder.btnEditar.setOnClickListener(v -> {
                listener.onEditarProducto(producto);
            });

            // Botón Eliminar
            holder.btnEliminar.setOnClickListener(v -> {
                listener.onEliminarProducto(producto);
            });

            // Aseguramos que los botones estén visibles en el módulo "Mis Anuncios"
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);

        } else {
            // Si el listener es nulo (ej: se usa el adaptador en el home), se ocultan los botones
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void actualizarProductos(List<Producto> nuevaLista) {
        this.listaProductos.clear();
        this.listaProductos.addAll(nuevaLista);
        this.listaOriginal.clear();
        this.listaOriginal.addAll(nuevaLista);
        notifyDataSetChanged();
        Log.d(TAG, "Lista de productos actualizada. Total: " + nuevaLista.size());
    }


    public void filtrar(String texto) {
        // ... (Tu código de filtrado)
        String textoBusqueda = texto.toLowerCase(Locale.getDefault()).trim();

        List<Producto> listaFiltrada = new ArrayList<>();

        if (textoBusqueda.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
            Log.d(TAG, "Búsqueda vacía. Mostrando todos los productos: " + listaFiltrada.size());
        } else {
            for (Producto producto : listaOriginal) {
                if (producto.getNombre().toLowerCase(Locale.getDefault()).contains(textoBusqueda)) {
                    listaFiltrada.add(producto);
                }
            }
            Log.d(TAG, "Productos encontrados para '" + texto + "': " + listaFiltrada.size());
        }

        this.listaProductos = listaFiltrada;
        notifyDataSetChanged();
    }


    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagenProducto;
        TextView tvNombre;
        TextView tvPrecio;
        ImageView btnEditar;
        ImageView btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vistas de producto
            ivImagenProducto = itemView.findViewById(R.id.iv_producto_imagen);
            tvNombre = itemView.findViewById(R.id.tv_producto_titulo);
            tvPrecio = itemView.findViewById(R.id.tv_producto_precio);

            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}