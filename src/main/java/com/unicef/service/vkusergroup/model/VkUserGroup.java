package com.unicef.service.vkusergroup.model;

import com.unicef.service.player.model.Player;
import lombok.Data;

@Data
public class VkUserGroup {
    private String vkGroupId;
    private Boolean isPlayer = false;
    private Long vkId;
    private Player player;


}
