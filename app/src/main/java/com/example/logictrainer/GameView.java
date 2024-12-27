package com.example.logictrainer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class GameView extends View {
    private int gridRows = 5;
    private int gridCols = 5;
    private float cellSize;
    private Paint gridPaint;
    private Paint piecePaint;
    private ArrayList<PuzzlePiece> puzzlePieces = new ArrayList<>();

    private PuzzlePiece draggedPiece = null;
    private float dragOffsetX, dragOffsetY;
    private float originalX, originalY; // Исходные координаты для возврата
    private boolean gameOver = false;

    private GameListener gameListener;
    private GestureDetector gestureDetector;

    public interface GameListener {
        void onPuzzleSolved();
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
    }

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gridPaint = new Paint();
        gridPaint.setColor(Color.CYAN);
        gridPaint.setStrokeWidth(3);
        gridPaint.setStyle(Paint.Style.STROKE);

        piecePaint = new Paint();
        piecePaint.setStyle(Paint.Style.FILL);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                handleDoubleTap(e.getX(), e.getY());
                return true;
            }
        });
    }

    public void setGridSize(int rows, int cols) {
        this.gridRows = rows;
        this.gridCols = cols;
        invalidate();
    }

    public void setPuzzlePieces(ArrayList<PuzzlePiece> pieces) {
        this.puzzlePieces = pieces;

        if (cellSize <= 0) {
            cellSize = Math.min(getWidth() / gridCols, getHeight() / gridRows);
        }

        float startX = 50;
        float startY = gridRows * cellSize + 20;

        for (PuzzlePiece piece : pieces) {
            piece.setPosition(startX, startY);
            startX += piece.getShape()[0].length * cellSize + 20;

            if (startX + piece.getShape()[0].length * cellSize > getWidth()) {
                startX = 50;
                startY += piece.getShape().length * cellSize + 20;
            }

            if (startY + piece.getShape().length * cellSize > getHeight()) {
                startY = gridRows * cellSize + 20;
            }
        }
        invalidate();
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);

        float width = getWidth();
        float height = getHeight();

        cellSize = Math.min(width / gridCols, height / gridRows);

        for (int i = 0; i <= gridRows; i++) {
            canvas.drawLine(0, i * cellSize, gridCols * cellSize, i * cellSize, gridPaint);
        }
        for (int j = 0; j <= gridCols; j++) {
            canvas.drawLine(j * cellSize, 0, j * cellSize, gridRows * cellSize, gridPaint);
        }

        for (PuzzlePiece piece : puzzlePieces) {
            piece.draw(canvas, piecePaint, cellSize);
        }

        if (gameOver) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Игра окончена", width / 2, height / 2, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) return true;

        gestureDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = puzzlePieces.size() - 1; i >= 0; i--) {
                    PuzzlePiece piece = puzzlePieces.get(i);
                    if (piece.contains(x, y, cellSize)) {
                        draggedPiece = piece;
                        dragOffsetX = x - piece.getX();
                        dragOffsetY = y - piece.getY();
                        originalX = piece.getX(); // Сохраняем исходные координаты
                        originalY = piece.getY();
                        puzzlePieces.remove(piece);
                        puzzlePieces.add(piece);
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggedPiece != null) {
                    draggedPiece.setPosition(x - dragOffsetX, y - dragOffsetY);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (draggedPiece != null) {
                    if (!trySnapToGrid(draggedPiece)) {
                        // Возвращаем фигуру на исходное место, если вставка невозможна
                        draggedPiece.setPosition(originalX, originalY);
                    }
                    invalidate();
                    if (checkPuzzleSolved() && gameListener != null) {
                        new Handler().postDelayed(() -> gameListener.onPuzzleSolved(), 500);
                    }
                    draggedPiece = null;
                }
                break;
        }

        return true;
    }

    private void handleDoubleTap(float x, float y) {
        for (int i = puzzlePieces.size() - 1; i >= 0; i--) {
            PuzzlePiece piece = puzzlePieces.get(i);
            if (piece.contains(x, y, cellSize)) {
                piece.rotate90Clockwise();
                invalidate();
                break;
            }
        }
    }

    private boolean trySnapToGrid(PuzzlePiece piece) {
        float gx = Math.round(piece.getX() / cellSize) * cellSize;
        float gy = Math.round(piece.getY() / cellSize) * cellSize;

        piece.setPosition(gx, gy);

        return canPlacePiece(piece);
    }

    private boolean canPlacePiece(PuzzlePiece piece) {
        boolean[][] board = new boolean[gridRows][gridCols];

        for (PuzzlePiece placedPiece : puzzlePieces) {
            if (placedPiece == piece) continue; // Пропускаем текущую фигуру
            boolean[][] shape = placedPiece.getShape();
            int startCol = (int) (placedPiece.getX() / cellSize);
            int startRow = (int) (placedPiece.getY() / cellSize);

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c]) {
                        int br = startRow + r;
                        int bc = startCol + c;

                        if (br >= 0 && br < gridRows && bc >= 0 && bc < gridCols) {
                            board[br][bc] = true;
                        }
                    }
                }
            }
        }

        boolean[][] shape = piece.getShape();
        int startCol = (int) (piece.getX() / cellSize);
        int startRow = (int) (piece.getY() / cellSize);

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c]) {
                    int br = startRow + r;
                    int bc = startCol + c;

                    if (br < 0 || br >= gridRows || bc < 0 || bc >= gridCols || board[br][bc]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkPuzzleSolved() {
        boolean[][] board = new boolean[gridRows][gridCols];

        for (PuzzlePiece piece : puzzlePieces) {
            boolean[][] shape = piece.getShape();
            int startCol = (int) (piece.getX() / cellSize);
            int startRow = (int) (piece.getY() / cellSize);

            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c]) {
                        int br = startRow + r;
                        int bc = startCol + c;

                        if (br < 0 || br >= gridRows || bc < 0 || bc >= gridCols || board[br][bc]) {
                            return false;
                        }
                        board[br][bc] = true;
                    }
                }
            }
        }

        for (boolean[] row : board) {
            for (boolean cell : row) {
                if (!cell) {
                    return false;
                }
            }
        }

        return true;
    }
}
