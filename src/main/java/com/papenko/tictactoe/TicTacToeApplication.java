package com.papenko.tictactoe;

import com.papenko.tictactoe.service.GameService;
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

        var context = SpringApplication.run(TicTacToeApplication.class, args);

        try {
            telegramBotsApi.registerBot(new SandoxBot(System.getenv("BOT_TOKEN"), context.getBean(GameService.class)));
        } catch (TelegramApiException e) {
            log.error("Something went wrong during bot registration", e);
        }
    }
}
