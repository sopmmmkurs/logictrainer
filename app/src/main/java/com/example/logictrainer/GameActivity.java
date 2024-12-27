package com.example.logictrainer;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements GameView.GameListener {
    private GameView gameView;
    private TextView levelTextView;
    private TextView timerTextView;
    private Button restartLevelButton;

    private LevelManager levelManager;
    private CountDownTimer timer;
    private boolean isLevelCompleted = false; // Флаг завершения уровня

    private DatabaseHelper databaseHelper;
    private int userId;
    private int userLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        levelTextView = findViewById(R.id.levelTextView);
        timerTextView = findViewById(R.id.timerTextView);
        gameView = findViewById(R.id.gameView);
        restartLevelButton = findViewById(R.id.restartLevelButton);

        databaseHelper = new DatabaseHelper(this);

        // Получаем ID и уровень пользователя из Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);
        userLevel = intent.getIntExtra("user_level", 1);

        levelManager = new LevelManager();
        levelManager.setCurrentLevel(userLevel);

        restartLevelButton.setOnClickListener(v -> restartLevel());

        startLevel();
    }

    private void startLevel() {
        isLevelCompleted = false; // Сбрасываем флаг завершения уровня
        gameView.post(() -> {
            gameView.setGameOver(false); // Сбрасываем статус окончания игры
            gameView.setGridSize(levelManager.getGridRows(), levelManager.getGridCols());
            gameView.setPuzzlePieces(levelManager.getPiecesForCurrentLevel());
            gameView.setGameListener(this);
            levelTextView.setText(getString(R.string.level_label, levelManager.getCurrentLevel()));

            startLevelTimer(levelManager.getLevelTimeLimit());
        });
    }

    private void restartLevel() {
        if (timer != null) {
            timer.cancel();
        }
        startLevel();
        Toast.makeText(this, "Уровень перезапущен", Toast.LENGTH_SHORT).show();
    }

    private void startLevelTimer(long timeLimitMillis) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(timeLimitMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(getString(R.string.time_label, (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                if (!isLevelCompleted && !isFinishing() && !isDestroyed()) {
                    isLevelCompleted = true;
                    gameView.setGameOver(true); // Устанавливаем статус окончания игры
                    timerTextView.setText(getString(R.string.time_up));
                    showGameOverDialog();
                }
            }
        }.start();
    }

    @Override
    public void onPuzzleSolved() {
        if (!isLevelCompleted && !isFinishing() && !isDestroyed()) {
            isLevelCompleted = true;
            Toast.makeText(this, "Вы выиграли! Переход на следующий уровень.", Toast.LENGTH_SHORT).show();

            // Инкремент уровня и сохранение
            int nextLevel = levelManager.getCurrentLevel() + 1;
            updateUserLevel(nextLevel);

            // Показ диалога выигрыша
            showWinDialog();
        }
    }


    private void updateUserLevel(int newLevel) {
        userLevel = newLevel; // Обновляем текущий уровень
        levelManager.setCurrentLevel(newLevel);

        // Обновляем уровень в базе данных
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_LEVEL, newLevel);

        int rowsUpdated = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});

        if (rowsUpdated > 0) {
            // Также обновляем SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("user_level", newLevel);
            editor.apply();
        } else {
            Toast.makeText(this, "Ошибка сохранения уровня", Toast.LENGTH_SHORT).show();
        }
    }


    private void showWinDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поздравляем!");
        builder.setMessage("Вы прошли уровень.");
        builder.setPositiveButton("Следующий уровень", (dialog, which) -> {
            startLevel(); // Просто запускаем следующий уровень
            dialog.dismiss();
        });
        builder.setNegativeButton("На главный экран", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }

    private void showGameOverDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Игра окончена");
        builder.setMessage("Вы проиграли.");
        builder.setPositiveButton("Рестарт", (dialog, which) -> {
            restartLevel();
            dialog.dismiss();
        });
        builder.setNegativeButton("На главный экран", (dialog, which) -> finish());
        builder.setCancelable(false);
        builder.show();
    }
}
