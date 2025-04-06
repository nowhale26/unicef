package com.unicef.bot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {
    private final String botToken;

    public TelegramBotConfig(BotConfig botConfig) {
        botToken = botConfig.telegramToken();
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botToken);
    }
}
