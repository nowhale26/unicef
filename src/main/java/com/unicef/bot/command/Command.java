package com.unicef.bot.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.unicef.service.telegramuser.TelegramUserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
public abstract class Command {

    @Autowired
    private TelegramBot bot;

    private String name;
    private String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract void execute(Update update);

    public void sendMessageToBot(Long tgChatId, String message) {
        bot.execute(new SendMessage(tgChatId, message));
    }

}
