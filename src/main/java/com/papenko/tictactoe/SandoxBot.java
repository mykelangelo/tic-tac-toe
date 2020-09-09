package com.papenko.tictactoe;

import com.papenko.tictactoe.entity.CellState;
import com.papenko.tictactoe.entity.GameData;
import com.papenko.tictactoe.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
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

    private AnswerInlineQuery getResponse(InlineQuery inlineQuery) {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(inlineQuery.getId());
        answerInlineQuery.setResults(provideTwoOptions(inlineQuery));
        return answerInlineQuery;
    }

    private List<InlineQueryResult> provideTwoOptions(InlineQuery inlineQuery) {
        InlineQueryResultArticle article0 = new InlineQueryResultArticle();
        article0.setInputMessageContent(new InputTextMessageContent()
                .setMessageText(inlineQuery.getFrom().getFirstName() + " goes first"));
        article0.setId("0");
        final GameData gameData = service.fetchGameData(inlineQuery.getId());
        article0.setReplyMarkup(new InlineKeyboardMarkup()
                .setKeyboard(getGameField(gameData, "1" + inlineQuery.getId())));
        article0.setTitle("Go first");
        article0.setDescription("Wanna go first? Click me!");
        article0.setThumbUrl("https://user-images.githubusercontent.com/46972880/" +
                "92474303-458e1b80-f1e4-11ea-99eb-14b00a8144f6.png");

        InlineQueryResultArticle article1 = new InlineQueryResultArticle();
        article1.setInputMessageContent(new InputTextMessageContent()
                .setMessageText(inlineQuery.getFrom().getFirstName() + " goes second"));
        article1.setId("1");
        article1.setReplyMarkup(new InlineKeyboardMarkup()
                .setKeyboard(getGameField(gameData, "2" + inlineQuery.getId())));
        article1.setTitle("Go second");
        article1.setDescription("Wanna go second? Click me!");
        article1.setThumbUrl("https://user-images.githubusercontent.com/46972880/" +
                "92474297-44f58500-f1e4-11ea-915e-a8961ea92496.png");

        log.info("first user: {}", inlineQuery.getFrom());
        service.addFirstUser(gameData, inlineQuery.getFrom());

        return List.of(article0, article1);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasInlineQuery()) {
            try {
                execute(getResponse(update.getInlineQuery()));
            } catch (TelegramApiException e) {
                log.error("could not execute (new inline game)", e);
            }
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            if ("/start".equals(messageText)) {
                GameData gameData = service.fetchGameData(GameData.createId(chatId, messageId));
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
            if (update.getCallbackQuery().getMessage() == null) {
                String id = update.getCallbackQuery().getInlineMessageId();
                String callData = update.getCallbackQuery().getData();
                if (callData.startsWith("c")) {
                    var order = Integer.parseInt(callData.substring(3, 4));
                    GameData gameData = service.fetchGameData(callData.substring(4));
                    log.info("user {}", update.getCallbackQuery().getFrom());
                    log.info("first user from db {}", gameData.getFirstUserId());
                    log.info("second user from db {}", gameData.getSecondUserId());
                    if (order == 2) {
                        if (gameData.getSecondUserId() == null) {
                            if (update.getCallbackQuery().getFrom().getId().equals(gameData.getFirstUserId())) {
                                try {
                                    log.info("first user attempts to go first, when he must be second");
                                    execute(new AnswerCallbackQuery()
                                            .setCallbackQueryId(update.getCallbackQuery().getId())
                                            .setShowAlert(true).setText("✋"));
                                } catch (TelegramApiException e) {
                                    log.error("could not execute (first user goes first, when he must be second)", e);
                                }
                                return;
                            } else {
                                service.addSecondUser(gameData, update.getCallbackQuery().getFrom());
                            }
                        }
                    } else {
                        if (gameData.getSecondUserId() == null) {
                            if (!update.getCallbackQuery().getFrom().getId().equals(gameData.getFirstUserId())) {
                                service.addSecondUser(gameData, update.getCallbackQuery().getFrom());
                                try {
                                    log.info("second user interrupts first move");
                                    execute(new AnswerCallbackQuery()
                                            .setCallbackQueryId(update.getCallbackQuery().getId())
                                            .setShowAlert(true).setText("✋"));
                                } catch (TelegramApiException e) {
                                    log.error("could not execute (second user interrupts first move)", e);
                                }
                                return;
                            }
                        }
                    }
                    if (!update.getCallbackQuery().getFrom().getId().equals(gameData.getFirstUserId()) &&
                            update.getCallbackQuery().getFrom().getId().equals(gameData.getSecondUserId())) {
                        try {
                            log.info("third user interrupts");
                            execute(new AnswerCallbackQuery()
                                    .setCallbackQueryId(update.getCallbackQuery().getId())
                                    .setShowAlert(true).setText("✋"));
                        } catch (TelegramApiException e) {
                            log.error("could not execute (third user)", e);
                        }
                        return;
                    }
                    var x = Integer.valueOf(callData.substring(1, 2));
                    var y = Integer.valueOf(callData.substring(2, 3));
                    if (service.makeMove(x, y, gameData)) {
                        final boolean xMove = gameData.getCurrentState() == CellState.X;
                        var message = new EditMessageText()
                                .setInlineMessageId(id)
                                .setText((xMove ? gameData.getFirstUserName() : gameData.getSecondUserName())
                                        + " \uD83C\uDFC6 (" + swapState(gameData.getCurrentState()) + ") won" +
                                        (xMove ? gameData.getSecondUserName() : gameData.getFirstUserName()) +
                                                " \uD83D\uDE2D !\n" + gameData);

                        try {
                            execute(message);
                        } catch (TelegramApiException e) {
                            log.error("could not execute (game finished)", e);
                        }
                    } else {
                        if (!gameData.isMoveInProgress()) {
                            var markup = new InlineKeyboardMarkup()
                                    .setKeyboard(getGameField(gameData, callData.substring(3)));

                            var message = new EditMessageText()
                                    .setInlineMessageId(id)
                                    .setText(swapMessage(gameData.getCurrentState()))
                                    .setReplyMarkup(markup);

                            try {
                                execute(message);
                            } catch (TelegramApiException e) {
                                log.error("could not execute (game in progress)", e);
                            }
                        }
                    }
                }
                return;
            }
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callData = update.getCallbackQuery().getData();
            if (callData.startsWith("c")) {
                var x = Integer.valueOf(callData.substring(1, 2));
                var y = Integer.valueOf(callData.substring(2, 3));
                GameData gameData = service.fetchGameData(GameData.createId(chatId, messageId));

                if (service.makeMove(x, y, gameData)) {
                    var message = new EditMessageText()
                            .setChatId(chatId)
                            .setMessageId(toIntExact(messageId))
                            .setText(swapState(gameData.getCurrentState()) + " won!");

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("could not execute (game finished)", e);
                    }
                } else {
                    if (!gameData.isMoveInProgress()) {
                        var markup = new InlineKeyboardMarkup().setKeyboard(getGameField(gameData));

                        var message = new EditMessageText()
                                .setChatId(chatId)
                                .setMessageId(toIntExact(messageId))
                                .setText(swapMessage(gameData.getCurrentState()))
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
    }

    private String swapMessage(CellState cellState) {
        return cellState == CellState.O ? O_S_TURN : X_S_TURN;
    }

    private String swapState(CellState cellState) {
        return cellState == CellState.O ? CellState.X.toString() : CellState.O.toString();
    }

    private static List<List<InlineKeyboardButton>> getGameField(GameData gameData) {
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

    private static List<List<InlineKeyboardButton>> getGameField(GameData gameData, String order) {
        var c00 = new InlineKeyboardButton().setText(gameData.getC00().toString()).setCallbackData("c00" + order);
        var c01 = new InlineKeyboardButton().setText(gameData.getC01().toString()).setCallbackData("c01" + order);
        var c02 = new InlineKeyboardButton().setText(gameData.getC02().toString()).setCallbackData("c02" + order);

        var c10 = new InlineKeyboardButton().setText(gameData.getC10().toString()).setCallbackData("c10" + order);
        var c11 = new InlineKeyboardButton().setText(gameData.getC11().toString()).setCallbackData("c11" + order);
        var c12 = new InlineKeyboardButton().setText(gameData.getC12().toString()).setCallbackData("c12" + order);

        var c20 = new InlineKeyboardButton().setText(gameData.getC20().toString()).setCallbackData("c20" + order);
        var c21 = new InlineKeyboardButton().setText(gameData.getC21().toString()).setCallbackData("c21" + order);
        var c22 = new InlineKeyboardButton().setText(gameData.getC22().toString()).setCallbackData("c22" + order);

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
