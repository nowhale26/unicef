package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.externalapi.hse.HseService;
import org.springframework.stereotype.Component;

@Component
public class HseAuthCodeCommand extends Command {

    private final HseService hseService;

    public HseAuthCodeCommand(HseService hseService) {
        super("/hseauthcode", "Set HSE auth code");
        this.hseService = hseService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        String[] updateMessage = update.message().text().split("\\s+");
        if (updateMessage.length < 2) {
            sendMessageToBot(tgChatId, "Usage: /hseauthcode <code>");
            return;
        }

        String code = updateMessage[1];
        boolean authorized = hseService.authorizeByCode(code);
        if (authorized) {
            sendMessageToBot(tgChatId, "HSE code accepted. Token saved.");
        } else {
            sendMessageToBot(tgChatId, "Failed to exchange code. Check if the code is valid and not expired.");
        }
    }
}
