package com.papenko.tictactoe.entity;

public enum CellCoordinates {
    C_00(0, 0), C_01(0, 1), C_02(0, 2),
    C_10(1, 0), C_11(1, 1), C_12(1, 2),
    C_20(2, 0), C_21(2, 1), C_22(2, 2);

    private final int x;
    private final int y;

    CellCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
