package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.externalapi.mosff.MosffService;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.vk.VkService;
import com.unicef.externalapi.vk.model.VkUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckGroupCommand extends Command {

    private final VkService vkService;

    public CheckGroupCommand(VkService vkService) {
        super("/checkGroup", "Изначальная проверка пользователей группы");
        this.vkService = vkService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        String[] updateMessage = update.message().text().split("\\s+");
        if (updateMessage.length == 2) {
            String groupName = updateMessage[1];
            vkService.checkGroup(groupName, true, tgChatId);
        }
    }
}
