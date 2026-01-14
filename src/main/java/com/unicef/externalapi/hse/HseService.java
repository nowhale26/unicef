package com.unicef.externalapi.hse;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.unicef.externalapi.hse.model.HseDumpEntity;
import com.unicef.externalapi.hse.model.HseTokenResponse;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.ufl.UflService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HseService {

    @Qualifier("taskExecutor")
    @Autowired
    private TaskExecutor taskExecutor;

    private final TelegramBot bot;
    private final HseClient hseClient;
    private volatile String accessToken;
    private volatile long accessTokenExpiresAt;

    public HseService(TelegramBot bot, HseClient hseClient) {
        this.bot = bot;
        this.hseClient = hseClient;
    }

    public boolean authorizeByCode(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            return false;
        }

        HseTokenResponse token = hseClient.requestTokenByCode(authorizationCode);
        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            return false;
        }

        this.accessToken = token.getAccessToken();
        this.accessTokenExpiresAt = resolveExpiresAt(token.getAccessExpiresIn());
        return true;
    }

    public HseDumpEntity findPersonWithStoredToken(String fullName) {
        if (!isTokenValid()) {
            return null;
        }

        List<HseDumpEntity> results = hseClient.searchByNameWithToken(fullName, accessToken);
        return findByName(fullName, results);
    }

    public HseDumpEntity findPerson(String fullName) {
        List<HseDumpEntity> results = hseClient.searchByName(fullName);
        return findByName(fullName, results);
    }

    private HseDumpEntity findByName(String fullName, List<HseDumpEntity> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        String normalizedFullName = normalizeName(fullName);

        for (HseDumpEntity entity : results) {
            String candidate = resolveName(entity);
            String normalizedCandidate = normalizeName(candidate);
            if (normalizedCandidate != null && normalizedCandidate.equalsIgnoreCase(normalizedFullName)) {
                return entity;
            }
        }

        // Если точного совпадения нет, проверяем совпадение по фамилии и имени
        String[] fullNameParts = normalizedFullName.trim().split("\\s+");
        if (fullNameParts.length >= 2) {
            String surnameFirstName = fullNameParts[0] + " " + fullNameParts[1];

            for (HseDumpEntity entity : results) {
                String candidate = resolveName(entity);
                String normalizedCandidate = normalizeName(candidate);
                if (normalizedCandidate != null) {
                    String[] candidateParts = normalizedCandidate.trim().split("\\s+");
                    if (candidateParts.length >= 2) {
                        String candidateSurnameFirstName = candidateParts[0] + " " + candidateParts[1];
                        if (candidateSurnameFirstName.equalsIgnoreCase(surnameFirstName)) {
                            return entity;
                        }
                    }
                }
            }
        }

        return null;
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
        return null;
    }

    private boolean isTokenValid() {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        if (accessTokenExpiresAt <= 0) {
            return true;
        }
        return System.currentTimeMillis() < accessTokenExpiresAt;
    }

    private long resolveExpiresAt(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return 0L;
        }
        long millis = expiresIn;
        if (expiresIn < 100000000L) {
            millis = expiresIn * 1000L;
        }
        return System.currentTimeMillis() + millis;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.replace('ё', 'е').replace('Ё', 'Е');
    }

    public void checkUflPlayersInAppX(Long tgChatId, List<MosffPlayer> uflPlayers){
        for (MosffPlayer player : uflPlayers) {
            taskExecutor.execute(() -> {
                processPlayerInAppX(player, tgChatId);
            });
        }
    }

    public void processPlayerInAppX(MosffPlayer player, Long tgChatId) {
        String fullName = player.getName();
        log.info("Имя игрока из юфл: {}",fullName);
        HseDumpEntity person = findPersonWithStoredToken(fullName);
        if(person != null){
            String response = formatResponse(person, player);
            bot.execute(new SendMessage(tgChatId, response));
        } else {
            log.info("Не найден в app x игрок с именем: {}", fullName);
        }
    }

    private String formatResponse(HseDumpEntity person, MosffPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("Имя: ").append(resolveName(person)).append("\n");
        if (person.getEmail() != null && !person.getEmail().isBlank()) {
            sb.append("Емэйл: ").append(person.getEmail()).append("\n");
        }
        if (person.getDescription() != null && !person.getDescription().isBlank()) {
            sb.append("Группа: ").append(person.getDescription()).append("\n");
        }
        if(player.getDateOfBirth() != null && !player.getDateOfBirth().isBlank()){
            sb.append("Дата рождения: ").append(player.getDateOfBirth()).append("\n");
        }
        if(player.getTeam() != null && !player.getTeam().isBlank()){
            sb.append("Команда: ").append(player.getTeam()).append("\n");
        }
        if(player.getPosition() != null && !player.getPosition().isBlank()){
            sb.append("Позиция: ").append(player.getPosition()).append("\n");
        }
        if (person.getAvatarUrl() != null && !person.getAvatarUrl().isBlank()) {
            sb.append("Avatar: ").append(person.getAvatarUrl()).append("\n");
        }
        return sb.toString().trim();
    }

}
