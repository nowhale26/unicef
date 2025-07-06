package com.unicef.service.player.repository;

import com.unicef.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity,Long> {
    Optional<PlayerEntity> findByVkId(Long vkId);
}
