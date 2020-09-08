package com.papenko.tictactoe.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "game")
public class GameData {
    @Id
    private String id;
    private CellState c00, c01, c02;
    private CellState c10, c11, c12;
    private CellState c20, c21, c22;
    private boolean moveInProgress;
    private Integer fromX;
    private Integer fromY;
    private CellState currentState;

    public GameData(String id) {
        this.id = id;

        c00 = CellState.EMPTY;
        c01 = CellState.EMPTY;
        c02 = CellState.EMPTY;

        c10 = CellState.EMPTY;
        c11 = CellState.EMPTY;
        c12 = CellState.EMPTY;

        c20 = CellState.EMPTY;
        c21 = CellState.EMPTY;
        c22 = CellState.EMPTY;
        currentState = CellState.X;
    }

    public static String createId(Long chatId, Long messageId) {
        return chatId + "|" + messageId;
    }

    @Override
    public String toString() {
        return "{state=" + currentState +
                ", id=" + id +
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

    public CellState getCellByCoordinates(Integer x, Integer y) {
        if (x == 0) {
            switch (y) {
                case 0:
                    return c00;
                case 1:
                    return c01;
                case 2:
                    return c02;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (x == 1) {
            switch (y) {
                case 0:
                    return c10;
                case 1:
                    return c11;
                case 2:
                    return c12;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (x == 2) {
            switch (y) {
                case 0:
                    return c20;
                case 1:
                    return c21;
                case 2:
                    return c22;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else {
            throw new IllegalArgumentException("x:" + x);
        }
    }

    public void setCellByCoordinates(Integer x, Integer y, CellState state) {
        if (x == 0) {
            switch (y) {
                case 0:
                    c00 = state;
                    return;
                case 1:
                    c01 = state;
                    return;
                case 2:
                    c02 = state;
                    return;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (x == 1) {
            switch (y) {
                case 0:
                    c10 = state;
                    return;
                case 1:
                    c11 = state;
                    return;
                case 2:
                    c12 = state;
                    return;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (x == 2) {
            switch (y) {
                case 0:
                    c20 = state;
                    return;
                case 1:
                    c21 = state;
                    return;
                case 2:
                    c22 = state;
                    return;
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else {
            throw new IllegalArgumentException("x:" + x);
        }
    }
}
