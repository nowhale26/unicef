package com.unicef.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.unicef.ApplicationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {
    private final String botToken;

    public TelegramBotConfig(ApplicationConfig applicationConfig) {
        botToken = applicationConfig.telegramToken();
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botToken);
    }
}
