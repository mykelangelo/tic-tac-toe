package com.papenko.tictactoe.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cell {
    private CellState cellState;
    private final CellCoordinates cellCoordinates;

    @Override
    public String toString() {
        return cellState.toString();
    }
}
