package ru.andryss.observer.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties("bot")
public class BotProperties {
    @NotBlank
    private String token;
    @NotBlank
    private String username;
}
