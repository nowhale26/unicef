package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.externalapi.hse.HseService;
import com.unicef.externalapi.ufl.UflService;
import org.springframework.stereotype.Component;

@Component
public class CheckUflCommand extends Command{

    private final UflService uflService;

    public CheckUflCommand(UflService uflService) {
        super("/checkufl", "Проверка всех игроков юфл в app x");
        this.uflService = uflService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        String messageText = update.message().text();
        String[] parts = messageText.split("\\s+");

        if (parts.length > 1) {
            try {
                int startPage = Integer.parseInt(parts[1]);
                // Если передали 0, считаем это первой страницей
                if (startPage == 0) {
                    startPage = 1;
                }
                uflService.getAndCheckAllUflPlayers(tgChatId, startPage);
            } catch (NumberFormatException e) {
                uflService.getAndCheckAllUflPlayers(tgChatId, 1);
            }
        } else {
            uflService.getAndCheckAllUflPlayers(tgChatId, 1);
        }
    }
}
