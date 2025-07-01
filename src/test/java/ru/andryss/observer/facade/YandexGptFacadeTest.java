package ru.andryss.observer.facade;

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.andryss.observer.config.YandexGptProperties;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.Alternative;
import ru.andryss.observer.generated.yandexgpt.model.CompletionOptions;
import ru.andryss.observer.generated.yandexgpt.model.CompletionRequest;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponseResult;
import ru.andryss.observer.generated.yandexgpt.model.Message;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions.ModeEnum;
import ru.andryss.observer.model.MessageDto;

class YandexGptFacadeTest {

    YandexGptApi yandexGptApi;
    YandexGptFacade yandexGptFacade;

    @BeforeEach
    void before() {
        yandexGptApi = Mockito.mock(YandexGptApi.class);

        YandexGptProperties properties = new YandexGptProperties();
        properties.setApiKey("mock-token");
        properties.setModelUri("mock-model-uri");
        properties.setDefaultModelTemperature(new BigDecimal("0.999"));
        properties.setDefaultModelMaxTokens(1);

        yandexGptFacade = new YandexGptFacade(properties, yandexGptApi);
    }

    @Test
    void testGenerateAlternativeSuccess() {
        Mockito.when(yandexGptApi.generateCompletions(Mockito.any()))
                .thenReturn(new CompletionResponse()
                        .result(new CompletionResponseResult()
                                .addAlternativesItem(new Alternative()
                                        .message(new Message()
                                                .role(MessageRole.ASSISTANT)
                                                .text("some response")
                                        )
                                        .status(Alternative.StatusEnum.FINAL)
                                )
                                .modelVersion("0.0.1")
                        )
                );

        String response = yandexGptFacade.generateAlternative(contextMessages(), userMessage());

        Assertions.assertThat(response).isEqualTo("some response");

        verifyCreateCompletion();
    }

    @Test
    void testGenerateFailedWithNoAlternatives() {
        Mockito.when(yandexGptApi.generateCompletions(Mockito.any()))
                .thenReturn(new CompletionResponse()
                        .result(new CompletionResponseResult()
                                .modelVersion("0.0.1")
                        )
                );

        Assertions.assertThatThrownBy(() -> yandexGptFacade.generateAlternative(contextMessages(), userMessage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No alternatives returned from yandex gpt");

        verifyCreateCompletion();
    }

    private List<MessageDto> contextMessages() {
        return List.of(
                new MessageDto(ru.andryss.observer.model.MessageRole.SYSTEM, "some-instuction"),
                new MessageDto(ru.andryss.observer.model.MessageRole.USER, "some-user-first-message"),
                new MessageDto(ru.andryss.observer.model.MessageRole.ASSISTANT, "some-model-answer")
        );
    }

    private MessageDto userMessage() {
        return new MessageDto(ru.andryss.observer.model.MessageRole.USER, "some-user-message");
    }

    private void verifyCreateCompletion() {
        Mockito.verify(yandexGptApi).generateCompletions(new CompletionRequest()
                .modelUri("mock-model-uri")
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(0.999)
                        .maxTokens("1")
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ModeEnum.DISABLED)
                        )
                )
                .messages(List.of(
                        new Message().role(MessageRole.SYSTEM).text("some-instuction"),
                        new Message().role(MessageRole.USER).text("some-user-first-message"),
                        new Message().role(MessageRole.ASSISTANT).text("some-model-answer"),
                        new Message().role(MessageRole.USER).text("some-user-message")
                ))
        );
    }

}