package com.papenko.tictactoe.entity;

import lombok.Value;

@Value
public class EatMove {
    CellCoordinates from;
    CellCoordinates to;
}