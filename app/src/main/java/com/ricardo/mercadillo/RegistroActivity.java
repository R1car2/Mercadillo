package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ricardo.mercadillo.R;

import com.google.android.material.textfield.TextInputEditText;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText etNombre;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnRegistro;
    private TextView tvIrLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.et_nombre);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegistro = findViewById(R.id.btn_registro);
        tvIrLogin = findViewById(R.id.tv_link_login);

        btnRegistro.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegistroActivity.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegistroActivity.this, "Registro simulado OK. Volviendo a Login.", Toast.LENGTH_SHORT).show();
                irALogin();
            }
        });

        tvIrLogin.setOnClickListener(v -> irALogin());
    }

    private void irALogin() {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
