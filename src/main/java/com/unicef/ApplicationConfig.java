package com.unicef;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ApplicationConfig(@NotEmpty String telegramToken, @NotEmpty String vkToken,
                                @NotEmpty String hseClientId, @NotEmpty String hseRedirectUri,
                                @NotEmpty String hseLogin, @NotEmpty String hsePassword,
                                @NotEmpty String minYear, @NotEmpty String maxYear) {
}
