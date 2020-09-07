package com.papenko.tictactoe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@Slf4j
@SpringBootApplication
public class TicTacToeApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        SpringApplication.run(TicTacToeApplication.class, args);
    }

}
