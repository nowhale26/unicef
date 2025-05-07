package com.unicef.service.telegramuser.repository;

import com.unicef.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TgUserRepository extends JpaRepository<TelegramUserEntity, Long> {
}
