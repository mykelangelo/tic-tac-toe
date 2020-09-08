package com.papenko.tictactoe;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputContactMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static java.lang.Math.toIntExact;

@Slf4j
@Component
public class SandoxBot extends TelegramLongPollingBot {
    private static final String O_S_TURN = "O's turn";
    private static final String X_S_TURN = "X's turn";
    private String botToken;

    private final GameService service;

    public SandoxBot(@Value("#{systemEnvironment['BOT_TOKEN']}") String botToken, GameService service) {
        this.botToken = botToken;
        this.service = service;
    }

    private static AnswerInlineQuery convertResultsToResponse(InlineQuery inlineQuery) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQuery.getId());
        answerInlineQuery.setResults(provideTwoOptions());
        return answerInlineQuery;
    }

    private static List<InlineQueryResult> provideTwoOptions() {
        InlineQueryResultArticle article0 = new InlineQueryResultArticle();
        article0.setInputMessageContent(new InputContactMessageContent());
        article0.setId("0");
        article0.setTitle("Go first");
        article0.setDescription("Whanna go first? Click me!");
        article0.setThumbUrl("https://pixabay.com/vectors/one-green-square-rounded-number-1-39418/");

        InlineQueryResultArticle article1 = new InlineQueryResultArticle();
        article1.setInputMessageContent(new InputContactMessageContent());
        article1.setId("1");
        article1.setTitle("Go second");
        article1.setDescription("Whanna go second? Click me!");
        article1.setThumbUrl("https://pixabay.com/vectors/two-blue-square-rounded-number-39419/");

        return List.of(article0, article1);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasInlineQuery()) {
            try {
                execute(convertResultsToResponse(update.getInlineQuery()));
            } catch (TelegramApiException e) {
                log.error("could not execute (new inline game)", e);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            if ("/start".equals(messageText)) {
                GameData gameData = service.fetchGameData(chatId, messageId);
                var markup = new InlineKeyboardMarkup().setKeyboard(getGameField(gameData));

                var message = new SendMessage()
                        .setChatId(chatId)
                        .setText(X_S_TURN + '(' + update.getMessage().getChat().getFirstName() + ')')
                        .setReplyMarkup(markup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("could not execute (new game)", e);
                }
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

                    var message = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(toIntExact(messageId))
                            .setText(swapMessage(messageText))
                            .setReplyMarkup(markup);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("could not execute (game in progress)", e);
                    }
                }
            }
        }
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
}
