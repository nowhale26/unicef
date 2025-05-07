package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.service.telegramuser.TelegramUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends Command {

    private final TelegramUserService userService;

    public StartCommand(TelegramUserService userService) {
        super( "/start", "Регистарция чата");
        this.userService = userService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        try {
            userService.registerUser(tgChatId);
        } catch (Exception e) {
            sendMessageToBot(tgChatId, e.getMessage());
        }
        sendMessageToBot(tgChatId, "Чат успешно зарегистрирован");
    }
}
