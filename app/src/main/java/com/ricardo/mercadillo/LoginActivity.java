package com.ricardo.mercadillo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.ricardo.mercadillo.HomeActivity;
import com.ricardo.mercadillo.RegistroActivity;
import com.ricardo.mercadillo.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asegúrate de que este layout exista en res/layout/
        setContentView(R.layout.activity_login);

        // 1. Inicialización de Vistas
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvIrRegistro = findViewById(R.id.tv_link_registro);

        // A. Botón de Login (Simulación para probar Intent)
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                // Validación mínima de la interfaz
                Toast.makeText(LoginActivity.this, "Por favor, ingresa email y contraseña.", Toast.LENGTH_SHORT).show();
            } else {
                // Simulación de inicio de sesión exitoso y prueba del Intent
                Toast.makeText(LoginActivity.this, "Simulación OK. Navegando a Home.", Toast.LENGTH_SHORT).show();
                irAHome();
            }
        });

        // B. Navegación a la pantalla de Registro
        tvIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    public void irAHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
