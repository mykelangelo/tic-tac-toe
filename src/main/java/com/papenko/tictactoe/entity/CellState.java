package com.papenko.tictactoe.entity;

public enum CellState {
    EMPTY("□"), X("X"), O("O");

    private String stringRepresentation;

    CellState(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
