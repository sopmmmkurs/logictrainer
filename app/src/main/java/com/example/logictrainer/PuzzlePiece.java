package com.example.logictrainer;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PuzzlePiece {
    private boolean[][] shape;
    private float x, y; // позиция левого верхнего угла фигуры
    private int color; // Цвет фигуры

    public PuzzlePiece(boolean[][] shape, int color) {
        this.shape = shape;
        this.color = color;
    }

    public void draw(Canvas canvas, Paint paint, float cellSize) {
        paint.setColor(color); // Устанавливаем цвет фигуры
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c]) {
                    float left = x + c * cellSize;
                    float top = y + r * cellSize;
                    float right = left + cellSize;
                    float bottom = top + cellSize;
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
    }

    public boolean contains(float touchX, float touchY, float cellSize) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c]) {
                    float left = x + c * cellSize;
                    float top = y + r * cellSize;
                    float right = left + cellSize;
                    float bottom = top + cellSize;
                    if (touchX >= left && touchX <= right && touchY >= top && touchY <= bottom) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public boolean[][] getShape() { return shape; }

    public int getColor() { return color; }

    public void rotate90Clockwise() {
        int rows = shape.length;
        int cols = shape[0].length;
        boolean[][] rotated = new boolean[cols][rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = shape[r][c];
            }
        }
        shape = rotated;
    }
}