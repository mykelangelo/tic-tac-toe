package com.papenko.tictactoe.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.papenko.tictactoe.entity.CellState.EMPTY;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameData {
    private GameId id;
    private Cell c00, c01, c02;
    private Cell c10, c11, c12;
    private Cell c20, c21, c22;

    public GameData(Integer chatId, Integer gameCounter) {
        id = new GameId(chatId, gameCounter);

        c00 = new Cell(EMPTY, CellCoordinates.C_00);
        c01 = new Cell(EMPTY, CellCoordinates.C_01);
        c02 = new Cell(EMPTY, CellCoordinates.C_02);

        c10 = new Cell(EMPTY, CellCoordinates.C_10);
        c11 = new Cell(EMPTY, CellCoordinates.C_11);
        c12 = new Cell(EMPTY, CellCoordinates.C_12);

        c20 = new Cell(EMPTY, CellCoordinates.C_20);
        c21 = new Cell(EMPTY, CellCoordinates.C_21);
        c22 = new Cell(EMPTY, CellCoordinates.C_22);
    }

    @Override
    public String toString() {
        return "{" +
                "chatId=" + id.getChatId() +
                ", gameCounter=" + id.getGameCounter() +
                '\n' + c00 +
                '|' + c01 +
                '|' + c02 +
                '\n' + c10 +
                '|' + c11 +
                '|' + c12 +
                '\n' + c20 +
                '|' + c21 +
                '|' + c22 +
                "\n}";
    }
}
