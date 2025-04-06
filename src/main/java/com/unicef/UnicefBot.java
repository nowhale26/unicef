package com.unicef;

import com.unicef.bot.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class UnicefBot {

	public static void main(String[] args) {
		SpringApplication.run(UnicefBot.class, args);
	}

}
