package ru.andryss.observer.config;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration of Yandex GPT API usage. Documentation details:
 * <a href="https://yandex.cloud/ru/docs/foundation-models/api-ref/authentication">authentication</a>
 * <a href="https://yandex.cloud/ru/docs/foundation-models/concepts/yandexgpt/models#addressing-models">model</a>
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("yandexgpt")
public class YandexGptProperties {
    @NotBlank
    private String apiKey;
    @NotBlank
    private String modelUri;
    @Min(3)
    private int contextMessagesCount = 21;
    @NotBlank
    private String defaultModelInstruction = """
            Будь как хороший друг — общайся просто, по-человечески, без официоза.
            Отвечай кратко и по сути, не больше пары предложений, не занудствуй.
            Поддерживай разговор, интересуйся собеседником, шутки и мемы — по настроению.
            Не строй из себя всезнайку.
            """;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal defaultModelTemperature = new BigDecimal("0.7");
    @Positive
    private int defaultModelMaxTokens = 200;
}
