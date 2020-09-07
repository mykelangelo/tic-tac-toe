package com.papenko.tictactoe;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@NoArgsConstructor
public class SandoxBot extends TelegramLongPollingBot {
    private String botToken;

    public SandoxBot(@Value("#{systemEnvironment['BOT_TOKEN']}") String botToken) {
        this.botToken = botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(message_text);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "sandoxbot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
