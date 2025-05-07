package com.unicef.service.telegramuser;

import com.unicef.entity.TelegramUserEntity;
import com.unicef.service.telegramuser.model.TelegramUser;
import com.unicef.service.telegramuser.repository.TgUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TelegramUserService {
    private final TgUserRepository tgUserRepository;

    public TelegramUserService(TgUserRepository tgUserRepository) {
        this.tgUserRepository = tgUserRepository;
    }

    @Transactional
    public void registerUser(Long tgChatId) {
        TelegramUserEntity tgUserEntity = new TelegramUserEntity();
        tgUserEntity.setTgChatId(tgChatId);
        tgUserRepository.save(tgUserEntity);
    }
}
