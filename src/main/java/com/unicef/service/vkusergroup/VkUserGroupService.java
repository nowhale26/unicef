package com.unicef.service.vkusergroup;

import com.unicef.entity.PlayerEntity;
import com.unicef.entity.VkUserGroupEntity;
import com.unicef.externalapi.vk.model.VkUser;
import com.unicef.service.player.PlayerService;
import com.unicef.service.player.model.Player;
import com.unicef.service.vkusergroup.model.VkUserGroup;
import com.unicef.service.vkusergroup.repository.VkUserGroupRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VkUserGroupService {
    private final VkUserGroupRepository vkUserGroupRepository;
    private final PlayerService playerService;

    public VkUserGroupService(VkUserGroupRepository vkUserGroupRepository, PlayerService playerService) {
        this.vkUserGroupRepository = vkUserGroupRepository;
        this.playerService = playerService;
    }

    @Transactional
    public VkUserGroupEntity saveVkUser(VkUserGroup vkUser){
        Optional<VkUserGroupEntity> existingEntity = vkUserGroupRepository.findByVkId(
                vkUser.getVkId());

        if (existingEntity.isPresent()) {
            return existingEntity.get();
        }

        VkUserGroupEntity entity = new VkUserGroupEntity();
        entity.setVkId(vkUser.getVkId());
        entity.setVkGroupId(vkUser.getVkGroupId());
        entity.setIsPlayer(vkUser.getIsPlayer());

        Player player = vkUser.getPlayer();
        if (player != null) {
            PlayerEntity playerEntity = playerService.savePlayer(player);
            entity.setPlayer(playerEntity);
        }

        vkUserGroupRepository.save(entity);
        return entity;
    }

    public boolean existsUserWithThisVkId(VkUser vkUser){
        Optional<VkUserGroupEntity> existingEntity = vkUserGroupRepository.findByVkId(
                vkUser.getId());
        return existingEntity.isPresent();
    }
}
