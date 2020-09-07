package com.papenko.tictactoe.service;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.entity.GameId;
import com.papenko.tictactoe.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository repository;

    public GameData fetchGameData(long chatId, long messageId) {
        return repository.findById(new GameId(chatId, messageId))
                .orElse(repository.save(new GameData(chatId, messageId)));
    }

    public static boolean isFree(GameData gameData, Integer x, Integer y) {
        return gameData.getCellByCoordinates(x, y).equals(CellState.EMPTY);
    }

    private boolean eat(GameData gameData, Integer x, Integer y) {
        CellState from = gameData.getCellByCoordinates(gameData.getFromX(), gameData.getFromY());
        if (from == gameData.getCellByCoordinates(x, y)) {
            return false;
        }
        gameData.setCellByCoordinates(gameData.getFromX(), gameData.getFromY(), CellState.EMPTY);
        gameData.setCellByCoordinates(x, y, from);
        repository.save(gameData);
        return true;
    }

    private void put(GameData gameData, Integer x, Integer y, CellState state) {
        gameData.setCellByCoordinates(x, y, state);
        repository.save(gameData);
    }

    public void makeMove(CellState state, Integer x, Integer y, GameData gameData) {
        if (gameData.isMoveInProgress()) {
            if (eat(gameData, x, y)) {
                gameData.setMoveInProgress(false);
            }
        } else {
            gameData.setMoveInProgress(!GameService.isFree(gameData, x, y));
            if (gameData.isMoveInProgress()) {
                gameData.setFromX(x);
                gameData.setFromY(y);
            } else {
                put(gameData, x, y, state);
            }
        }
    }
}
