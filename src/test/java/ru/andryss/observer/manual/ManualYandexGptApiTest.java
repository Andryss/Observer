package ru.andryss.observer.manual;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.CompletionOptions;
import ru.andryss.observer.generated.yandexgpt.model.CompletionRequest;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.Message;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions;

@Slf4j
@Disabled("For manual testing only")
public class ManualYandexGptApiTest extends BaseDbTest {

    private static final String IAM_TOKEN = "mock-token";
    private static final String FOLDER_ID = "mock-folder";

    private YandexGptApi initApi() {
        YandexGptApi yandexGptApi = new YandexGptApi();
        yandexGptApi.getApiClient().setBearerToken(IAM_TOKEN);
        return yandexGptApi;
    }

    @Test
    void testCompletion() {
        YandexGptApi yandexGptApi = initApi();

        CompletionRequest request = new CompletionRequest()
                .modelUri("gpt://%s/yandexgpt-lite/latest".formatted(FOLDER_ID))
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(0.3)
                        .maxTokens("200")
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ReasoningOptions.ModeEnum.DISABLED)
                        )
                )
                .messages(List.of(
                        new Message()
                                .role(MessageRole.SYSTEM)
                                .text("You are chat bot"),
                        new Message()
                                .role(MessageRole.USER)
                                .text("Hi! How are you?")
                ));

        CompletionResponse response = yandexGptApi.generateCompletions(request);

        log.info("{}", response);
    }

}
