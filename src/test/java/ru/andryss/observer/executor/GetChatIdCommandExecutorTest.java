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
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class GetChatIdCommandExecutorTest extends BaseDbTest {

    @Autowired
    GetChatIdCommandExecutor executor;

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
        keyStorageService.put("getChatIdCommandExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessNoMessage() {
        Update update = new Update();

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessEntitiesNull() {
        Message message = new Message();
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessEntitiesEmpty() {
        Message message = new Message();
        message.setEntities(List.of());
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessEntitiesHasNoCommand() {
        Message message = new Message();
        message.setText("@username #hashtag $USD https://url.com hi!");
        message.setEntities(List.of(
                new MessageEntity("mention", 0, 9),
                new MessageEntity("hashtag", 10, 8),
                new MessageEntity("cashtag", 19, 4),
                new MessageEntity("url", 24, 15)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessEntitiesHasAnotherCommand() {
        Message message = new Message();
        message.setText("/start");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 6)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessCommandWithoutMention() {
        Message message = new Message();
        message.setText("prefix /chatid suffix");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 7, 7)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    void testCanProcessCommandWithMention() {
        Message message = new Message();
        message.setText("prefix /chatid@bot suffix");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 7, 11)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessEmptyBlacklist() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        executor.process(update, sender);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting("chatId", "text")
                .containsExactly("123", "123");
        Mockito.verifyNoMoreInteractions(sender);
    }

}