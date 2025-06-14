package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class SendMessageExecutorTest extends BaseDbTest {

    @Autowired
    SendMessageExecutor executor;

    @Autowired
    KeyStorageService keyStorageService;

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
        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(123L));

        executor.process(buildUpdate(), sender);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting(
                        "chatId",
                        "text",
                        "replyToMessageId",
                        "allowSendingWithoutReply"
                )
                .containsExactly(
                        "123",
                        "some-text",
                        456,
                        true
                );
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