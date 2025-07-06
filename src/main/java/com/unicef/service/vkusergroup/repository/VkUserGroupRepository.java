package com.unicef.service.vkusergroup.repository;

import com.unicef.entity.PlayerEntity;
import com.unicef.entity.VkUserGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VkUserGroupRepository extends JpaRepository<VkUserGroupEntity, Long> {
    Optional<VkUserGroupEntity> findByVkId(Long vkId);
}
