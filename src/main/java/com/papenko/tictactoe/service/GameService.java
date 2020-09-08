package com.papenko.tictactoe.service;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository repository;

    public GameData fetchGameData(String id) {
        return repository.findById(id)
                .orElse(repository.save(new GameData(id)));
    }

    public static boolean isFree(GameData gameData, Integer x, Integer y) {
        return gameData.getCellByCoordinates(x, y) == CellState.EMPTY;
    }

    private boolean eat(GameData gameData, Integer x, Integer y) {
        CellState from = gameData.getCellByCoordinates(gameData.getFromX(), gameData.getFromY());
        if (from != gameData.getCurrentState()) {
            log.info("starting state is not {}", gameData.getCurrentState());
            gameData.setMoveInProgress(false);
            return false;
        }
        CellState to = gameData.getCellByCoordinates(x, y);
        if (from == to) {
            log.info("cannot eat the piece of the same type {}", from);
            return false;
        }
        if (to == CellState.EMPTY) {
            log.info("to cell is empty");
            return false;
        }
        if (!isEatMoveAllowed(gameData.getFromX(), gameData.getFromY(), x, y)) {
            log.info("move is not allowed");
            return false;
        }
        gameData.setCellByCoordinates(gameData.getFromX(), gameData.getFromY(), CellState.EMPTY);
        gameData.setCellByCoordinates(x, y, from);
        return true;
    }

    private boolean isEatMoveAllowed(Integer fromX, Integer fromY, Integer x, Integer y) {
        if (fromX == 0) {
            switch (fromY) {
                case 0:
                    return (x == 1 && y == 0) || (y == 1 && x == 0);
                case 1:
                    return x == 1 && y == 1;
                case 2:
                    return (x == 1 && y == 2) || (y == 1 && x == 0);
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (fromX == 1) {
            switch (fromY) {
                case 0:
                case 2:
                    return x == 1 && y == 1;
                case 1:
                    return (x == 0 && y == 0) || (x == 2 && y == 2) || (x == 0 && y == 2) || (x == 2 && y == 0);
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else if (fromX == 2) {
            switch (fromY) {
                case 0:
                    return (x == 1 && y == 0) || (x == 2 && y == 1);
                case 1:
                    return x == 1 && y == 1;
                case 2:
                    return (x == 1 && y == 2) || (x == 2 && y == 1);
                default:
                    throw new IllegalArgumentException("y:" + y);
            }
        } else {
            throw new IllegalArgumentException("x:" + x);
        }
    }

    private void put(GameData gameData, Integer x, Integer y) {
        gameData.setCellByCoordinates(x, y, gameData.getCurrentState());
    }

    public boolean makeMove(Integer x, Integer y, GameData gameData) {
        log.info("field: {}, x: {}, y: {}", gameData, x, y);
        if (gameData.isMoveInProgress()) {
            log.info("move was in progress, about to finish it");
            if (eat(gameData, x, y)) {
                log.info("move has finished!");
                gameData.setMoveInProgress(false);
                alterCurrentState(gameData);
                repository.save(gameData);
                final boolean gameFinished = isGameFinished(gameData);
                if (gameFinished) {
                    repository.delete(gameData);
                }
                return gameFinished;
            }
        }
        log.info("about to start new move");
        gameData.setMoveInProgress(!GameService.isFree(gameData, x, y));
        if (gameData.isMoveInProgress()) {
            log.info("started a new move!");
            gameData.setFromX(x);
            gameData.setFromY(y);
        } else {
            log.info("put a new piece");
            put(gameData, x, y);
            alterCurrentState(gameData);
        }
        repository.save(gameData);
        final boolean gameFinished = isGameFinished(gameData);
        if (gameFinished) {
            repository.delete(gameData);
        }
        return gameFinished;
    }

    private static boolean isGameFinished(GameData data) {
        if (data.getC00() != CellState.EMPTY) {
            if ((data.getC00() == data.getC01() && data.getC00() == data.getC02()) ||
                    (data.getC00() == data.getC10() && data.getC00() == data.getC20())) {
                if (data.getC11() == data.getC00() || data.getC11() == CellState.EMPTY) {
                    return true;
                }
            }
        }
        if (data.getC22() != CellState.EMPTY) {
            if ((data.getC22() == data.getC20() && data.getC21() == data.getC22()) ||
                    (data.getC22() == data.getC02() && data.getC22() == data.getC12())) {
                if (data.getC11() == data.getC22() || data.getC11() == CellState.EMPTY) {
                    return true;
                }
            }
        }
        if (data.getC11() != CellState.EMPTY) {
            if ((data.getC00() == data.getC11() && data.getC22() == data.getC11()) ||
                    (data.getC11() == data.getC02() && data.getC20() == data.getC11())) {
                if ((data.getC01() == CellState.EMPTY && data.getC10() == CellState.EMPTY &&
                        data.getC21() == CellState.EMPTY && data.getC12() == CellState.EMPTY) ||
                        !(data.getC01() != data.getC11() || data.getC10() != data.getC11() ||
                                data.getC21() != data.getC11() || data.getC12() != data.getC11())) {
                    return true;
                }
            }
            if ((data.getC01() == data.getC11() && data.getC11() == data.getC21()) ||
                    (data.getC10() == data.getC11() && data.getC11() == data.getC12())) {
                if (!(data.getC00() != data.getC11() || data.getC20() != data.getC11() ||
                        data.getC22() != data.getC11() || data.getC02() != data.getC11())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void alterCurrentState(GameData gameData) {
        gameData.setCurrentState(gameData.getCurrentState() == CellState.X ? CellState.O : CellState.X);
    }

    public void addFirstUser(GameData gameData, User first) {
        gameData.setFirstUser(first);
        repository.save(gameData);
        repository.refresh();
    }

    public void addSecondUser(GameData gameData, User first) {
        gameData.setSecondUser(first);
        repository.save(gameData);
        repository.refresh();
    }
}
