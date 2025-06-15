package ru.andryss.observer.facade;

import java.util.ArrayList;
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
import ru.andryss.observer.model.MessageDto;

/**
 * Facade for working with yandex gpt api
 */
@Service
@RequiredArgsConstructor
public class YandexGptFacade {

    private final YandexGptProperties properties;
    private final YandexGptApi yandexGptApi;

    /**
     * Invoke <a href="https://yandex.cloud/ru/docs/foundation-models/text-generation/api-ref/TextGeneration/
     * completion">generate completion method</a> of yandex gpt api with given parameters and extract model answer
     */
    public String generateAlternative(List<MessageDto> context, MessageDto userMessage) {
        ArrayList<Message> messages = new ArrayList<>();
        for (MessageDto messageDto : context) {
            messages.add(mapMessage(messageDto));
        }
        messages.add(mapMessage(userMessage));

        CompletionRequest request = new CompletionRequest()
                .modelUri(properties.getModelUri())
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(properties.getDefaultModelTemperature().doubleValue())
                        .maxTokens(String.valueOf(properties.getDefaultModelMaxTokens()))
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ModeEnum.DISABLED)
                        )
                )
                .messages(messages);

        CompletionResponse response = yandexGptApi.foundationModelsV1CompletionPost(request);

        List<Alternative> alternatives = response.getResult().getAlternatives();

        if (alternatives.isEmpty()) {
            throw new IllegalStateException("No alternatives returned from yandex gpt");
        }

        return alternatives.get(0).getMessage().getText();
    }

    private Message mapMessage(MessageDto dto) {
        return new Message()
                .role(mapRole(dto.getRole()))
                .text(dto.getText());
    }

    private MessageRole mapRole(ru.andryss.observer.model.MessageRole role) {
        return switch (role) {
            case SYSTEM -> MessageRole.SYSTEM;
            case USER -> MessageRole.USER;
            case ASSISTANT -> MessageRole.ASSISTANT;
        };
    }
}
