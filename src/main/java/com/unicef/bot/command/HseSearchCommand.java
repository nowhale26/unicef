package com.unicef.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.unicef.externalapi.hse.HseService;
import com.unicef.externalapi.hse.model.HseDumpEntity;
import org.springframework.stereotype.Component;

@Component
public class HseSearchCommand extends Command {

    private final HseService hseService;

    public HseSearchCommand(HseService hseService) {
        super("/hsesearch", "Search HSE person by full name");
        this.hseService = hseService;
    }

    @Override
    public void execute(Update update) {
        Long tgChatId = update.message().chat().id();
        String text = update.message().text();
        String fullName = text.replaceFirst("^/hsesearch\\s+", "").trim();

        if (fullName.isBlank()) {
            sendMessageToBot(tgChatId, "Usage: /hsesearch <ФИО>");
            return;
        }

        HseDumpEntity person = hseService.findPersonWithStoredToken(fullName);
        if (person == null) {
            sendMessageToBot(tgChatId, "Not found or token missing/expired.");
            return;
        }

        String response = formatResponse(person);
        sendMessageToBot(tgChatId, response);
    }

    private String formatResponse(HseDumpEntity person) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(resolveName(person)).append("\n");
        if (person.getEmail() != null && !person.getEmail().isBlank()) {
            sb.append("Email: ").append(person.getEmail()).append("\n");
        }
        if (person.getDescription() != null && !person.getDescription().isBlank()) {
            sb.append("Description: ").append(person.getDescription()).append("\n");
        }
        if (person.getType() != null && !person.getType().isBlank()) {
            sb.append("Type: ").append(person.getType()).append("\n");
        }
        if (person.getAvatarUrl() != null && !person.getAvatarUrl().isBlank()) {
            sb.append("Avatar: ").append(person.getAvatarUrl()).append("\n");
        }
        return sb.toString().trim();
    }

    private String resolveName(HseDumpEntity entity) {
        if (entity.getFullName() != null && !entity.getFullName().isBlank()) {
            return entity.getFullName();
        }
        if (entity.getLabel() != null && !entity.getLabel().isBlank()) {
            return entity.getLabel();
        }
        if (entity.getRoom() != null && !entity.getRoom().isBlank()) {
            return entity.getRoom();
        }
        return "Unknown";
    }
}
