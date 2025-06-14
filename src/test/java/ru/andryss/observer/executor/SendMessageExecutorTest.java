package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
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
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.Alternative;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponseResult;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.service.KeyStorageService;

import static org.mockito.ArgumentMatchers.any;

class SendMessageExecutorTest extends BaseDbTest {

    @Autowired
    SendMessageExecutor executor;

    @Autowired
    KeyStorageService keyStorageService;

    @Autowired
    YandexGptApi yandexGptApi;

    @MockitoBean
    AbsSender sender;

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
                                .modelVersion("0.0.1")
                        )
                );

        executor.process(buildUpdate(), sender);

        Mockito.verify(yandexGptApi).foundationModelsV1CompletionPost(any());

        ArgumentCaptor<SendChatAction> sendChatActionCaptor = ArgumentCaptor.forClass(SendChatAction.class);
        Mockito.verify(sender).execute(sendChatActionCaptor.capture());
        Assertions.assertThat(sendChatActionCaptor.getValue())
                .extracting(
                        "chatId",
                        "action"
                )
                .containsExactly(
                        "123",
                        "typing"
                );

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(sendMessageCaptor.capture());
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

        Mockito.verifyNoMoreInteractions(yandexGptApi);
        Mockito.verifyNoMoreInteractions(sender);
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
}