package com.example.logictrainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LevelManager {
    private int currentLevel = 1;

    // Фиксированное сопоставление типов фигур и цветов
    private static final Map<String, Integer> SHAPE_COLORS = new HashMap<>();

    static {
        SHAPE_COLORS.put("SingleCell", 0xFFFF0000); // Красный
        SHAPE_COLORS.put("Square", 0xFF00FF00);    // Зеленый
        SHAPE_COLORS.put("Line", 0xFF0000FF);      // Синий
        SHAPE_COLORS.put("LShape", 0xFFFFFF00);    // Желтый
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getGridRows() {
        return 5; // Размер сетки: 5x5
    }

    public int getGridCols() {
        return 5;
    }

    public long getLevelTimeLimit() {
        return 60 * 1000; // Для всех уровней даём 60 секунд
    }

    public ArrayList<PuzzlePiece> getPiecesForCurrentLevel() {
        ArrayList<PuzzlePiece> pieces = new ArrayList<>();
        int totalCells = getGridRows() * getGridCols(); // Общее количество клеток в сетке
        int targetCells = totalCells; // Цель - заполнить всё поле

        Random random = new Random();

        if (currentLevel == 1) {
            // Первый уровень: 4 квадратные фигуры и остальное - одиночные клетки
            int squarePieces = 4; // Количество квадратов
            for (int i = 0; i < squarePieces; i++) {
                PuzzlePiece squarePiece = new PuzzlePiece(createSquareShape(4), SHAPE_COLORS.get("Square"));
                pieces.add(squarePiece);
                targetCells -= 4; // Каждый квадрат занимает 4 клетки
            }

            while (targetCells > 0) {
                PuzzlePiece singleCellPiece = new PuzzlePiece(createSingleCellShape(), SHAPE_COLORS.get("SingleCell"));
                pieces.add(singleCellPiece);
                targetCells -= 1; // Одна клетка на каждую фигуру
            }
        } else {
            // Для следующих уровней: сложная генерация фигур
            while (targetCells > 0) {
                int shapeSize = Math.min(targetCells, random.nextInt(5) + 1); // Генерируем размер фигуры от 1 до 5 клеток
                PuzzlePiece piece = generateShapeForLevel(shapeSize);
                pieces.add(piece);
                targetCells -= countCellsInShape(piece.getShape());
            }
        }

        return pieces;
    }

    private PuzzlePiece generateShapeForLevel(int size) {
        Random random = new Random();
        int shapeType = random.nextInt(4); // 0 - квадрат, 1 - линия, 2 - L, 3 - одиночная клетка

        switch (shapeType) {
            case 0:
                return new PuzzlePiece(createSquareShape(size), SHAPE_COLORS.get("Square"));
            case 1:
                return new PuzzlePiece(createLineShape(size), SHAPE_COLORS.get("Line"));
            case 2:
                return new PuzzlePiece(createLShape(size), SHAPE_COLORS.get("LShape"));
            default:
                return new PuzzlePiece(createSingleCellShape(), SHAPE_COLORS.get("SingleCell"));
        }
    }

    private boolean[][] createSquareShape(int size) {
        int side = (int) Math.sqrt(size); // Вычисляем размер стороны квадрата
        boolean[][] square = new boolean[side][side];
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                square[r][c] = true;
            }
        }
        return square;
    }

    private boolean[][] createLineShape(int size) {
        boolean[][] line = new boolean[1][size]; // Линия горизонтальная
        for (int c = 0; c < size; c++) {
            line[0][c] = true;
        }
        return line;
    }

    private boolean[][] createLShape(int size) {
        int rows = Math.max(2, size / 2); // Минимум 2 строки
        int cols = Math.max(2, size - rows); // Минимум 2 столбца
        boolean[][] lShape = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            lShape[r][0] = true; // Вертикальная часть
        }
        for (int c = 0; c < cols; c++) {
            lShape[rows - 1][c] = true; // Горизонтальная часть
        }
        return lShape;
    }

    private boolean[][] createSingleCellShape() {
        return new boolean[][]{{true}};
    }

    private int countCellsInShape(boolean[][] shape) {
        int count = 0;
        for (boolean[] row : shape) {
            for (boolean cell : row) {
                if (cell) {
                    count++;
                }
            }
        }
        return count;
    }
}

































