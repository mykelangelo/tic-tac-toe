package com.papenko.tictactoe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@SpringBootApplication
public class TicTacToeApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            telegramBotsApi.registerBot(new SandoxBot(System.getenv("BOT_TOKEN"), null));
        } catch (TelegramApiException e) {
            log.error("Something went wrong during bot registration", e);
        }

        SpringApplication.run(TicTacToeApplication.class, args);
    }

}
