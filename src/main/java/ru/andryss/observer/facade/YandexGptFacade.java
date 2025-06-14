package ru.andryss.observer.facade;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.andryss.observer.config.YandexGptProperties;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.Alternative;
import ru.andryss.observer.generated.yandexgpt.model.CompletionOptions;
import ru.andryss.observer.generated.yandexgpt.model.CompletionRequest;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.Message;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions.ModeEnum;

/**
 * Facade for working with yandex gpt api
 */
@Service
@RequiredArgsConstructor
public class YandexGptFacade {

    private static final String MODEL_URI_TEMPLATE = "gpt://%s/yandexgpt";
    private static final double DEFAULT_MODEL_TEMPERATURE = 0.7;
    private static final String DEFAULT_MODEL_MAX_TOKENS = "200";
    private static final String DEFAULT_MODEL_INSTRUCTIONS = """
            Будь как хороший друг — общайся просто, по-человечески, без официоза.
            Отвечай кратко и по сути, не занудствуй.
            Поддерживай разговор, интересуйся собеседником, шутки и мемы — по настроению.
            Не строй из себя всезнайку, но если можешь помочь — помоги.
            """;

    private final YandexGptProperties properties;
    private final YandexGptApi yandexGptApi;

    /**
     * Invoke <a href="https://yandex.cloud/ru/docs/foundation-models/text-generation/api-ref/TextGeneration/
     * completion">generate completion method</a> of yandex gpt api with given parameters and extract model answer
     */
    public String generateAlternative(String text) {
        CompletionRequest request = new CompletionRequest()
                .modelUri(MODEL_URI_TEMPLATE.formatted(properties.getFolderId()))
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(DEFAULT_MODEL_TEMPERATURE)
                        .maxTokens(DEFAULT_MODEL_MAX_TOKENS)
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ModeEnum.DISABLED)
                        )
                )
                .messages(List.of(
                        new Message()
                                .role(MessageRole.SYSTEM)
                                .text(DEFAULT_MODEL_INSTRUCTIONS),
                        new Message()
                                .role(MessageRole.USER)
                                .text(text)
                ));

        CompletionResponse response = yandexGptApi.foundationModelsV1CompletionPost(request);

        List<Alternative> alternatives = response.getResult().getAlternatives();

        if (alternatives.isEmpty()) {
            throw new IllegalStateException("No alternatives returned from yandex gpt");
        }

        return alternatives.get(0).getMessage().getText();
    }
}
