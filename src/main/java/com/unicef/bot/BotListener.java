package com.unicef.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.unicef.bot.command.Command;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BotListener {

    private static final String UNKNOWN_COMMAND = "Неизвестная команда";
    private static final String USE_COMMAND = "Используйте команду (/...)";

    private final TelegramBot bot;

    private final BotCommand[] botCommandsForRegistration;

    private final Map<String, Command> commands = new HashMap<>();

    public BotListener(TelegramBot bot, List<Command> commands) {
        botCommandsForRegistration = new BotCommand[commands.size()];
        for (int i =0;i<commands.size();i++) {
            Command command = commands.get(i);
            String name = command.getName();
            String description = command.getDescription();
            this.commands.put(name, command);
            botCommandsForRegistration[i] = new BotCommand(name, description);
        }
        this.bot = bot;
    }

    @PostConstruct
    public void init() {
        bot.execute(new SetMyCommands(botCommandsForRegistration));

        bot.setUpdatesListener(
                updates -> {
                    return handleUpdates(updates);
                },
                error -> {
                    if (error.response() != null) {
                        // got bad response from telegram
                        log.error("Error code: {}", error.response().errorCode());
                        log.error("Error description: {}", error.response().description());
                    } else {
                        // probably network error
                        error.printStackTrace();
                    }
                });
    }

    protected int handleUpdates(List<Update> updates) {
        try{
            for (var update : updates) {
                String text = update.message().text();
                Long tgChatId = update.message().chat().id();
                if (text.startsWith("/")) {
                    String commandName = text.split("\\s+")[0];
                    if (commands.containsKey(commandName)) {
                        Command command = commands.get(commandName);
                        command.execute(update);
                    } else {
                        bot.execute(new SendMessage(tgChatId, UNKNOWN_COMMAND));
                    }
                } else {
                    bot.execute(new SendMessage(tgChatId, USE_COMMAND));
                }
            }
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
