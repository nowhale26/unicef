package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.common.validator.BachelorPlayerValidator;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.ufl.UflService;
import com.unicef.externalapi.vk.VkService;
import org.springframework.stereotype.Component;

@Component
public class CheckUserCommand extends Command {

    private final VkService vkService;

    public CheckUserCommand(VkService vkService) {
        super("/checkuser", "Проверка друзей пользователя");
        this.vkService = vkService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        String[] updateMessage = update.message().text().split("\\s+");
        if (updateMessage.length == 2) {
            String userVkId = updateMessage[1];
            vkService.checkGroup(userVkId, true, tgChatId, false, BachelorPlayerValidator.INSTANCE);
        }
    }
}
