package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.Alternative;
import ru.andryss.observer.generated.yandexgpt.model.CompletionRequest;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponseResult;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.service.KeyStorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class SendMessageExecutorTest extends BaseDbTest {

    private static final String SYSTEM_INSTRUCTION_TEXT = """
            Будь как хороший друг — общайся просто, по-человечески, без официоза.
            Отвечай кратко и по сути, не больше пары предложений, не занудствуй.
            Поддерживай разговор, интересуйся собеседником, шутки и мемы — по настроению.
            Не строй из себя всезнайку.
            """;

    @Autowired
    SendMessageExecutor executor;

    @Autowired
    KeyStorageService keyStorageService;

    @Autowired
    YandexGptApi yandexGptApi;

    @MockitoBean
    AbsSender sender;

    @BeforeEach
    void before() {
        Mockito.clearInvocations(yandexGptApi);
    }

    @Test
    void testIsActiveDefault() {
        Assertions.assertThat(executor.isActive()).isFalse();
    }

    @Test
    void testIsActiveKeySet() {
        keyStorageService.put("sendMessageExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessNoMessage() {
        Update update = new Update();

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessNoMessageText() {
        Message message = new Message();
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessNotPrivateMessage() {
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("some-text");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessAllowedChatsIsEmpty() {
        Assertions.assertThat(executor.canProcess(buildUpdate())).isFalse();
    }

    @Test
    void testCanProcessChatNotInAllowedChats() {
        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(456L));

        Assertions.assertThat(executor.canProcess(buildUpdate())).isFalse();
    }

    @Test
    void testCanProcessChatInAllowedChats() {
        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(123L));

        Assertions.assertThat(executor.canProcess(buildUpdate())).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessMessageSent() {
        mockYandexGptApiResponse();

        executor.process(buildUpdate(), sender);

        verifyYandexGptApiRequest(1,
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.SYSTEM)
                        .text(SYSTEM_INSTRUCTION_TEXT),
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.USER)
                        .text("some-text")
        );
        verifyTypingEventSent(1);
        verifyMessageSent(1);

        verifyNoMoreMocksInteractions();
    }

    @Test
    @SneakyThrows
    void testProcessWithContextSaved() {
        mockYandexGptApiResponse();

        executor.process(buildUpdate(), sender);
        executor.process(buildUpdate(), sender);

        verifyYandexGptApiRequest(2,
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.SYSTEM)
                        .text(SYSTEM_INSTRUCTION_TEXT),
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.USER)
                        .text("some-text"),
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.ASSISTANT)
                        .text("some response"),
                new ru.andryss.observer.generated.yandexgpt.model.Message()
                        .role(MessageRole.USER)
                        .text("some-text")
        );
        verifyTypingEventSent(2);
        verifyMessageSent(2);

        verifyNoMoreMocksInteractions();
    }

    private static Update buildUpdate() {
        Chat chat = new Chat();
        chat.setId(123L);
        chat.setType("private");

        Message message = new Message();
        message.setMessageId(456);
        message.setText("some-text");
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private void mockYandexGptApiResponse() {
        Mockito.when(yandexGptApi.foundationModelsV1CompletionPost(any()))
                .thenReturn(new CompletionResponse()
                        .result(new CompletionResponseResult()
                                .addAlternativesItem(new Alternative()
                                        .message(new ru.andryss.observer.generated.yandexgpt.model.Message()
                                                .role(MessageRole.ASSISTANT)
                                                .text("some response")
                                        )
                                        .status(Alternative.StatusEnum.FINAL)
                                )
                        )
                );
    }

    private void verifyYandexGptApiRequest(int count, ru.andryss.observer.generated.yandexgpt.model.Message... messages) {
        ArgumentCaptor<CompletionRequest> completionCaptor = ArgumentCaptor.forClass(CompletionRequest.class);
        Mockito.verify(yandexGptApi, times(count)).foundationModelsV1CompletionPost(completionCaptor.capture());
        Assertions.assertThat(completionCaptor.getValue()).satisfies(request ->
                Assertions.assertThat(request.getMessages()).containsExactly(messages)
        );
    }

    private void verifyTypingEventSent(int count) throws TelegramApiException {
        ArgumentCaptor<SendChatAction> sendChatActionCaptor = ArgumentCaptor.forClass(SendChatAction.class);
        Mockito.verify(sender, times(count)).execute(sendChatActionCaptor.capture());
        Assertions.assertThat(sendChatActionCaptor.getValue())
                .extracting(
                        "chatId",
                        "action"
                )
                .containsExactly(
                        "123",
                        "typing"
                );
    }

    private void verifyMessageSent(int count) throws TelegramApiException {
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender, times(count)).execute(sendMessageCaptor.capture());
        Assertions.assertThat(sendMessageCaptor.getValue())
                .extracting(
                        "chatId",
                        "text",
                        "replyToMessageId",
                        "allowSendingWithoutReply"
                )
                .containsExactly(
                        "123",
                        "some response",
                        456,
                        true
                );
    }

    private void verifyNoMoreMocksInteractions() {
        Mockito.verifyNoMoreInteractions(yandexGptApi);
        Mockito.verifyNoMoreInteractions(sender);
    }
}