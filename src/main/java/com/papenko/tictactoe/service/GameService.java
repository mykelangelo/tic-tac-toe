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
            log.info("cannot eat the piece of the same type {}", from);
            return false;
        }
        gameData.setCellByCoordinates(gameData.getFromX(), gameData.getFromY(), CellState.EMPTY);
        gameData.setCellByCoordinates(x, y, from);
        return true;
    }

    private void put(GameData gameData, Integer x, Integer y, CellState state) {
        gameData.setCellByCoordinates(x, y, state);
    }

    public void makeMove(CellState state, Integer x, Integer y, GameData gameData) {
        log.info("field: {}, state: {}, x: {}, y: {}", gameData, state, x, y);
        if (gameData.isMoveInProgress()) {
            log.info("move was in progress, about to finish it");
            if (eat(gameData, x, y)) {
                log.info("move has finished!");
                gameData.setMoveInProgress(false);
            }
        } else {
            log.info("about to start new move");
            gameData.setMoveInProgress(!GameService.isFree(gameData, x, y));
            if (gameData.isMoveInProgress()) {
                log.info("started a new move!");
                gameData.setFromX(x);
                gameData.setFromY(y);
            } else {
                log.info("put a new piece");
                put(gameData, x, y, state);
            }
        }
        repository.save(gameData);
    }
}
