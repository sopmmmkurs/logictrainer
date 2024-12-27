package com.example.logictrainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    private DatabaseHelper databaseHelper;

    private static final String SHARED_PREFS = "user_prefs";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";
    private static final String PREF_USER_LEVEL = "user_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Проверка авторизации
        if (isUserLoggedIn()) {
            redirectToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        // Инициализация полей и кнопок
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        databaseHelper = new DatabaseHelper(this);

        // Слушатели для кнопок
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Проверка на пустые поля
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = databaseHelper.getReadableDatabase();
            cursor = db.query(DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_LEVEL},
                    DatabaseHelper.COLUMN_EMAIL + "=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=?",
                    new String[]{email, password},
                    null, null, null);

            if (cursor.moveToFirst()) {
                // Получение ID, уровня и почты пользователя
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LEVEL));

                // Сохранение данных пользователя в SharedPreferences
                saveUserSession(userId, email, level);

                // Переход к главному экрану
                redirectToMainActivity();
            } else {
                Toast.makeText(this, "Неверные данные", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    private void saveUserSession(int userId, String email, int level) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_USER_ID, userId);
        editor.putString(PREF_USER_EMAIL, email);
        editor.putInt(PREF_USER_LEVEL, level);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.contains(PREF_USER_ID);
    }

    private void redirectToMainActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(PREF_USER_ID, -1);
        String email = sharedPreferences.getString(PREF_USER_EMAIL, null);
        int level = sharedPreferences.getInt(PREF_USER_LEVEL, 1);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user_id", userId);
        intent.putExtra("user_email", email);
        intent.putExtra("user_level", level);
        startActivity(intent);
        finish();
    }
}
