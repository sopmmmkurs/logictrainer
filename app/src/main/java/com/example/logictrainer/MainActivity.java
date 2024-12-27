package com.example.logictrainer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button startGameButton, logoutButton;
    private TextView userInfoTextView;

    private int userId;
    private String userEmail;
    private int userLevel;

    private static final String SHARED_PREFS = "user_prefs";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";
    private static final String PREF_USER_LEVEL = "user_level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startGameButton = findViewById(R.id.startGameButton);
        logoutButton = findViewById(R.id.logoutButton);
        userInfoTextView = findViewById(R.id.userInfoTextView);

        // Получение данных пользователя из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(PREF_USER_ID, -1);
        userEmail = sharedPreferences.getString(PREF_USER_EMAIL, null);
        userLevel = sharedPreferences.getInt(PREF_USER_LEVEL, 1);

        // Установка информации о пользователе
        updateUserInfo();

        // Слушатель для кнопки старта игры
        startGameButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
            gameIntent.putExtra("user_id", userId);
            gameIntent.putExtra("user_level", userLevel);
            startActivity(gameIntent);
        });

        // Слушатель для кнопки выхода
        logoutButton.setOnClickListener(v -> {
            clearUserSession();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Обновляем информацию о пользователе из SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        userLevel = sharedPreferences.getInt(PREF_USER_LEVEL, 1); // Обновляем уровень
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (userEmail != null) {
            userInfoTextView.setText("Почта: " + userEmail + "\nУровень: " + userLevel);
        } else {
            userInfoTextView.setText("Не удалось загрузить данные пользователя");
        }
    }

    // Метод для очистки данных сессии
    private void clearUserSession() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
