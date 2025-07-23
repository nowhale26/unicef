package com.unicef.externalapi.vk;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.unicef.common.validator.CorrectPlayerValidator;
import com.unicef.externalapi.mosff.MosffService;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.ufl.UflService;
import com.unicef.externalapi.vk.model.VkAnswer;
import com.unicef.externalapi.vk.model.VkResponse;
import com.unicef.externalapi.vk.model.VkUser;
import com.unicef.service.vkusergroup.VkUserGroupService;
import com.unicef.service.player.model.Player;
import com.unicef.service.vkusergroup.model.VkUserGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class VkService {

    private static final String getMembersUrl = "/groups.getMembers";
    public static final String getUserFriendsUrl = "/friends.get";

    private final VkClient vkClient;
    private final MosffService mosffService;
    private final TelegramBot bot;
    private final VkUserGroupService vkUserGroupService;

    @Qualifier("taskExecutor")
    @Autowired
    private TaskExecutor taskExecutor;
    private final UflService uflService;

    public VkService(VkClient vkClient, MosffService mosffService, TelegramBot bot, VkUserGroupService vkUserGroupService, UflService uflService) {
        this.vkClient = vkClient;
        this.mosffService = mosffService;
        this.bot = bot;
        this.vkUserGroupService = vkUserGroupService;
        this.uflService = uflService;
    }

    public VkResponse getGroupMembers(int offset, String groupId, boolean groupSearch) {
        VkAnswer response;
        if (groupSearch) {
            response = vkClient.getVkMembers(offset, groupId, getMembersUrl);
        } else {
            response = vkClient.getVkMembers(offset, groupId, getUserFriendsUrl);
        }
        return response.getResponse();
    }

    public void checkGroup(String groupId, boolean initialCheck, Long tgChatId, boolean groupSearch, CorrectPlayerValidator playerValidator) {
        VkResponse response = getGroupMembers(0, groupId, groupSearch);
        Long groupCapacity = response.getCount();
        if (initialCheck) {
            List<String> playersFromVkGroup = checkUsersInitial(response.getItems(), groupId, playerValidator);
            sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
            playersFromVkGroup.clear();
            for (int i = 1000; i < groupCapacity; i += 1000) {
                response = getGroupMembers(i, groupId, groupSearch);
                playersFromVkGroup = checkUsersInitial(response.getItems(), groupId, playerValidator);
                sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
                playersFromVkGroup.clear();
            }
            sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
        }
    }

    public List<String> checkUsersInitial(List<VkUser> vkUsers, String groupId, CorrectPlayerValidator playerValidator) {
        List<String> playersToTg = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger activeTasksCount = new AtomicInteger(0);

        activeTasksCount.addAndGet(vkUsers.size());

        for (VkUser vkUser : vkUsers) {
            taskExecutor.execute(() -> {
                try {
                    processVkUser(vkUser, groupId, playersToTg, playerValidator);
                } finally {
                    if (activeTasksCount.decrementAndGet() == 0) {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Ошибка выполнения многопоточной обработки пользователей VK", e);
        }

        return playersToTg;
    }

    private void processVkUser(VkUser vkUser, String groupId, List<String> playersToTg, CorrectPlayerValidator playerValidator) {
        if (vkUser.getSex() != 1 && !vkUserGroupService.existsUserWithThisVkId(vkUser)) {
            boolean foundPlayer = false;
            String surnameName = vkUser.getLastName() + " " + vkUser.getFirstName();
            MosffPlayer mosffPlayer = mosffService.findPlayer(surnameName);
            VkUserGroup vkUserGroup = new VkUserGroup();
            Player player = null;
            if (mosffPlayer != null) {
                if (playerValidator.checkPlayer(vkUser, mosffPlayer)) {
                    player = initPlayer(mosffPlayer, player, vkUser);
                    vkUserGroup.setIsPlayer(true);
                    String playerInfo = "Мосфф: " + mosffPlayer.getName() + " " + mosffPlayer.getPosition() + " " +
                            mosffPlayer.getDateOfBirth() + " " + mosffPlayer.getTeam() + " vk.com/id" + vkUser.getId();
                    playersToTg.add(playerInfo);
                    foundPlayer = true;
                    log.info("Найден игрок в мосфф: " + playerInfo);
                } else {
                    log.info("Не пройдена валидация мосфф vkId: " + vkUser.getId() + " День рождения: " + (vkUser.getBdate() != null ? vkUser.getBdate() : "Нет bdate") + " " + mosffPlayer.getDateOfBirth());
                    log.info("Университет: " + ((vkUser.getUniversities() != null && !vkUser.getUniversities().isEmpty()) ? vkUser.getUniversities().get(0).getName() : "Нет университета"));
                }
            }
            if (!foundPlayer) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Ошибка при добавлении задержки", e);
                }
                MosffPlayer uflPlayer = uflService.findPlayer(surnameName);
                if (uflPlayer != null) {
                    if (playerValidator.checkPlayer(vkUser, uflPlayer)) {
                        player = initPlayer(uflPlayer, player, vkUser);
                        vkUserGroup.setIsPlayer(true);
                        String playerInfo = "Юфл: " + uflPlayer.getName() + " " +
                                uflPlayer.getDateOfBirth() + " " + uflPlayer.getTeam() + " vk.com/id" + vkUser.getId();
                        playersToTg.add(playerInfo);
                        log.info("Найден игрок в юфл: " + playerInfo);
                    } else {
                        log.info("Не пройдена валидация юфл vkId: " + vkUser.getId() + " День рождения: " + (vkUser.getBdate() != null ? vkUser.getBdate() : "Нет bdate") + " " + uflPlayer.getDateOfBirth());
                        log.info("Университет: " + ((vkUser.getUniversities() != null && !vkUser.getUniversities().isEmpty()) ? vkUser.getUniversities().get(0).getName() : "Нет университета"));
                    }
                }
            }

            vkUserGroup.setPlayer(player);
            vkUserGroup.setVkId(vkUser.getId());
            vkUserGroup.setVkGroupId(groupId);
            vkUserGroupService.saveVkUser(vkUserGroup);
            log.info(vkUserGroup.toString());
        }
        log.info("Неподходящий пользователь vkId: " + vkUser.getId());
    }

    public void sendPlayerListToTelegram(List<String> playersFromVkGroup, Long tgChatId, String groupId) {
        if (!playersFromVkGroup.isEmpty()) {
            StringBuilder playerList = new StringBuilder(groupId + ":\n");
            for (var player : playersFromVkGroup) {
                playerList.append(player).append("\n");
            }
            bot.execute(new SendMessage(tgChatId, playerList.toString()));
        }
    }

    private Player initPlayer(MosffPlayer mosffPlayer, Player player, VkUser vkUser) {
        player = new Player();
        int birthYear = Integer.parseInt(mosffPlayer.getDateOfBirth().split("\\.")[2]);
        player.setBirthYear(birthYear);
        player.setVkId(vkUser.getId());
        player.setFirstName(vkUser.getFirstName());
        player.setLastName(vkUser.getLastName());
        return player;
    }
}
