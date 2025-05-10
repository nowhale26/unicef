package com.unicef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationConfig.class})
public class UnicefBot {

	public static void main(String[] args) {
		SpringApplication.run(UnicefBot.class, args);
	}

}
