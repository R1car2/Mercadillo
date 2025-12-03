package com.ricardo.mercadillo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ricardo.mercadillo.R; // Asegúrate de que R.drawable.agregar_img exista aquí
import java.util.List;

public class CarruselImagenAdapter extends RecyclerView.Adapter<CarruselImagenAdapter.ImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls;
    // Usa la imagen de placeholder que tienes
    private final int DEFAULT_IMAGE = R.drawable.agregar_img;

    public CarruselImagenAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el nuevo layout item_carrusel_imagen.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel_imagen, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Carga la imagen usando Glide
        Glide.with(context)
                .load(imageUrl)
                .placeholder(DEFAULT_IMAGE)
                .error(DEFAULT_IMAGE)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // El ID debe coincidir con item_carrusel_imagen.xml
            imageView = itemView.findViewById(R.id.item_imagen_carrusel);
        }
    }
}