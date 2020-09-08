package com.papenko.tictactoe;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.service.GameService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static java.lang.Math.toIntExact;

@Component
public class SandoxBot extends TelegramWebhookBot {
    private static final String O_S_TURN = "O's turn";
    private static final String X_S_TURN = "X's turn";
    private String botToken;

    private final GameService service;

    public SandoxBot(@Value("#{systemEnvironment['BOT_TOKEN']}") String botToken, GameService service) {
        this.botToken = botToken;
        this.service = service;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            if ("/start".equals(messageText)) {
                GameData gameData = service.fetchGameData(chatId, messageId);
                var markup = new InlineKeyboardMarkup().setKeyboard(getGameField(gameData));

                return new SendMessage() // Create a message object object
                        .setChatId(chatId)
                        .setText(X_S_TURN + '(' + update.getMessage().getChat().getFirstName() + ')')
                        .setReplyMarkup(markup);
            }
        } else if (update.hasCallbackQuery()) {
            String messageText = update.getCallbackQuery().getMessage().getText();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callData = update.getCallbackQuery().getData();
            if (callData.startsWith("c")) {
                var x = Integer.valueOf(callData.substring(1, 2));
                var y = Integer.valueOf(callData.substring(2, 3));
                GameData gameData = service.fetchGameData(chatId, messageId);

                var state = messageText.startsWith("X") ? CellState.X : CellState.O;
                service.makeMove(state, x, y, gameData);

                if (!gameData.isMoveInProgress()) {
                    var markup = new InlineKeyboardMarkup().setKeyboard(getGameField(gameData));

                    return new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(toIntExact(messageId))
                            .setText(swapMessage(messageText))
                            .setReplyMarkup(markup);
                }
            }
        }
        return null;
    }

    private String swapMessage(String messageText) {
        return messageText.startsWith("X") ? O_S_TURN : X_S_TURN;
    }

    private List<List<InlineKeyboardButton>> getGameField(GameData gameData) {
        var c00 = new InlineKeyboardButton().setText(gameData.getC00().toString()).setCallbackData("c00");
        var c01 = new InlineKeyboardButton().setText(gameData.getC01().toString()).setCallbackData("c01");
        var c02 = new InlineKeyboardButton().setText(gameData.getC02().toString()).setCallbackData("c02");

        var c10 = new InlineKeyboardButton().setText(gameData.getC10().toString()).setCallbackData("c10");
        var c11 = new InlineKeyboardButton().setText(gameData.getC11().toString()).setCallbackData("c11");
        var c12 = new InlineKeyboardButton().setText(gameData.getC12().toString()).setCallbackData("c12");

        var c20 = new InlineKeyboardButton().setText(gameData.getC20().toString()).setCallbackData("c20");
        var c21 = new InlineKeyboardButton().setText(gameData.getC21().toString()).setCallbackData("c21");
        var c22 = new InlineKeyboardButton().setText(gameData.getC22().toString()).setCallbackData("c22");

        return List.of(List.of(c00, c01, c02), List.of(c10, c11, c12), List.of(c20, c21, c22));
    }

    @Override
    public String getBotUsername() {
        return "sandoxbot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return "sandoxbot";
    }
}
