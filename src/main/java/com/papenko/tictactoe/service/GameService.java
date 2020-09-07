package com.papenko.tictactoe.service;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository repository;

    public GameData fetchGameData(long chatId, long messageId) {
        return repository.findById(GameData.createId(chatId, messageId))
                .orElse(repository.save(new GameData(chatId, messageId)));
    }

    public static boolean isFree(GameData gameData, Integer x, Integer y) {
        return gameData.getCellByCoordinates(x, y) == CellState.EMPTY;
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
        log.info("field: {}, state: {}, x: {}, y: {}", gameData, state, x, y);
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
