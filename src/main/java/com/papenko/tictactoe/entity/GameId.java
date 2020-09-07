package com.papenko.tictactoe.entity;

import lombok.Value;

@Value
public class GameId {
    Long chatId;
    Long messageId;
}
