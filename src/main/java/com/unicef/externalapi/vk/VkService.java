package com.unicef.externalapi.vk;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.unicef.externalapi.mosff.MosffService;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.vk.model.VkAnswer;
import com.unicef.externalapi.vk.model.VkResponse;
import com.unicef.externalapi.vk.model.VkUser;
import com.unicef.service.vkusergroup.VkUserGroupService;
import com.unicef.service.player.model.Player;
import com.unicef.service.vkusergroup.model.VkUserGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class VkService {

    private final VkClient vkClient;
    private final MosffService mosffService;
    private final TelegramBot bot;
    private final VkUserGroupService vkUserGroupService;

    public VkService(VkClient vkClient, MosffService mosffService, TelegramBot bot, VkUserGroupService vkUserGroupService) {
        this.vkClient = vkClient;
        this.mosffService = mosffService;
        this.bot = bot;
        this.vkUserGroupService = vkUserGroupService;
    }

    public VkResponse getGroupMembers(int offset, String groupId) {
        VkAnswer response = vkClient.getMembers(offset, groupId);
        return response.getResponse();
    }

    public void checkGroup(String groupId, boolean initialCheck, Long tgChatId) {
        VkResponse response = getGroupMembers(0, groupId);
        Long groupCapacity = response.getCount();
        if(initialCheck){
            List<String> playersFromVkGroup = checkUsersInitial(response.getItems(),groupId);
            sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
            playersFromVkGroup.clear();
            for(int i = 1000;i<groupCapacity;i+=1000){
                response = getGroupMembers(i,groupId);
                playersFromVkGroup = checkUsersInitial(response.getItems(),groupId);
                sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
                playersFromVkGroup.clear();
            }
            sendPlayerListToTelegram(playersFromVkGroup, tgChatId, groupId);
        }

    }

    public List<String> checkUsersInitial(List<VkUser> vkUsers, String groupId) {
        List<String> playersToTg = new ArrayList<>();
        for (var vkUser : vkUsers) {
            if(vkUser.getSex()!=1){
                String surnameName = vkUser.getLastName() + " " + vkUser.getFirstName();
                MosffPlayer mosffPlayer = mosffService.findPlayer(surnameName);
                VkUserGroup vkUserGroup = new VkUserGroup();
                Player player = null;
                if (mosffPlayer != null) {
                    player = new Player();
                    player.setBirthYear(Integer.parseInt(mosffPlayer.getDateOfBirth()));
                    player.setVkId(vkUser.getId());
                    player.setFirstName(vkUser.getFirstName());
                    player.setLastName(vkUser.getLastName());
                    vkUserGroup.setIsPlayer(true);
                    String playerInfo = mosffPlayer.getName() + " " + mosffPlayer.getPosition() + " " +
                            mosffPlayer.getDateOfBirth() + " " + mosffPlayer.getTeam()+" vk.com/id"+ vkUser.getId();
                    playersToTg.add(playerInfo);
                    log.info("Найден игрок: "+playerInfo);
                }
                vkUserGroup.setPlayer(player);
                vkUserGroup.setVkId(vkUser.getId());
                vkUserGroup.setVkGroupId(groupId);
                vkUserGroupService.saveVkUser(vkUserGroup);
                log.info(vkUserGroup.toString());
            }
        }
        return playersToTg;
    }
    public void sendPlayerListToTelegram(List<String> playersFromVkGroup, Long tgChatId, String groupId){
        if(!playersFromVkGroup.isEmpty()){
            StringBuilder playerList = new StringBuilder(groupId+":\n");
            for (var player : playersFromVkGroup){
                playerList.append(player).append("\n");
            }
            bot.execute(new SendMessage(tgChatId, playerList.toString()));
        }
    }
}
