package com.papenko.tictactoe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@SpringBootApplication
public class TicTacToeApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            telegramBotsApi.registerBot(new SandoxBot());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        SpringApplication.run(TicTacToeApplication.class, args);
    }

}
