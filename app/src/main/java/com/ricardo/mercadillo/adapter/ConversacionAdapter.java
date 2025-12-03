package com.ricardo.mercadillo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ricardo.mercadillo.ChatActivity;
import com.ricardo.mercadillo.R;
import com.ricardo.mercadillo.model.Conversacion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversacionAdapter extends RecyclerView.Adapter<ConversacionAdapter.ConversacionViewHolder> {

    private final Context context;
    private final List<Conversacion> listaConversaciones;

    public ConversacionAdapter(Context context, List<Conversacion> listaConversaciones) {
        this.context = context;
        this.listaConversaciones = listaConversaciones;
    }

    @NonNull
    @Override
    public ConversacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversacion, parent, false);
        return new ConversacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversacionViewHolder holder, int position) {
        Conversacion conversacion = listaConversaciones.get(position);

        // asignar datos de la conversación
        holder.tvNombreContacto.setText(conversacion.getNombreContacto());
        holder.tvUltimoMensaje.setText(conversacion.getUltimoMensaje());
        holder.tvHoraUltimaVez.setText(formatTimestamp(conversacion.getTimestamp()));



        // abrir ChatActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            // Pasamos el ID del receptor (el otro participante)
            intent.putExtra("ID_RECEPTOR", conversacion.getOtroUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaConversaciones.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ConversacionViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgAvatarContacto;
        final TextView tvNombreContacto;
        final TextView tvUltimoMensaje;
        final TextView tvHoraUltimaVez;

        public ConversacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatarContacto = itemView.findViewById(R.id.img_avatar_contacto);
            tvNombreContacto = itemView.findViewById(R.id.tv_nombre_contacto);
            tvUltimoMensaje = itemView.findViewById(R.id.tv_ultimo_mensaje);
            tvHoraUltimaVez = itemView.findViewById(R.id.tv_hora_ultima_vez);
        }
    }
}