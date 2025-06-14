package ru.andryss.observer.facade;

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

class YandexGptFacadeTest {

    YandexGptApi yandexGptApi;
    YandexGptFacade yandexGptFacade;

    @BeforeEach
    void before() {
        yandexGptApi = Mockito.mock(YandexGptApi.class);

        YandexGptProperties properties = new YandexGptProperties();
        properties.setIamToken("mock-token");
        properties.setFolderId("mock-folder");

        yandexGptFacade = new YandexGptFacade(properties, yandexGptApi);
    }

    @Test
    void testGenerateAlternativeSuccess() {
        Mockito.when(yandexGptApi.foundationModelsV1CompletionPost(Mockito.any()))
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

        String response = yandexGptFacade.generateAlternative("some-test");

        Assertions.assertThat(response).isEqualTo("some response");

        verifyCreateCompletion();
    }

    @Test
    void testGenerateFailedWithNoAlternatives() {
        Mockito.when(yandexGptApi.foundationModelsV1CompletionPost(Mockito.any()))
                .thenReturn(new CompletionResponse()
                        .result(new CompletionResponseResult()
                                .modelVersion("0.0.1")
                        )
                );

        Assertions.assertThatThrownBy(() -> yandexGptFacade.generateAlternative("some-test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No alternatives returned from yandex gpt");

        verifyCreateCompletion();
    }

    private void verifyCreateCompletion() {
        Mockito.verify(yandexGptApi).foundationModelsV1CompletionPost(new CompletionRequest()
                .modelUri("gpt://mock-folder/yandexgpt")
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(0.7)
                        .maxTokens("200")
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ModeEnum.DISABLED)
                        )
                )
                .messages(List.of(
                        new Message()
                                .role(MessageRole.SYSTEM)
                                .text("""
                                        Будь как хороший друг — общайся просто, по-человечески, без официоза.
                                        Отвечай кратко и по сути, не занудствуй.
                                        Поддерживай разговор, интересуйся собеседником, шутки и мемы — по настроению.
                                        Не строй из себя всезнайку, но если можешь помочь — помоги.
                                        """),
                        new Message()
                                .role(MessageRole.USER)
                                .text("some-test")
                ))
        );
    }

}