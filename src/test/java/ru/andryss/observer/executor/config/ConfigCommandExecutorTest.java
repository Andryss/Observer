package ru.andryss.observer.executor.config;

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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class ConfigCommandExecutorTest extends BaseDbTest {

    @Autowired
    ConfigCommandExecutor executor;

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
        keyStorageService.put("configCommandExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessNoMessageText() {
        Message message = new Message();
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessEmptyAdmins() {
        User user = new User();
        user.setId(123L);
        Message message = new Message();
        message.setFrom(user);
        message.setText("/config");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 7)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessUserIsAdmin() {
        User user = new User();
        user.setId(123L);
        Message message = new Message();
        message.setFrom(user);
        message.setText("/config");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 7)
        ));
        Update update = new Update();
        update.setMessage(message);

        keyStorageService.put("admin.userIds", List.of(123L));

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessNoArguments() {
        executor.process(buildUpdate(""), sender);

        verifySendMessage("Must be at least one arguments");
    }

    @Test
    @SneakyThrows
    void testProcessUnknownCommand() {
        executor.process(buildUpdate(" non-existent"), sender);

        verifySendMessage("Unknown command non-existent");
    }

    private Update buildUpdate(String text) {
        Chat chat = new Chat();
        chat.setId(456L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/config" + text);
        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private void verifySendMessage(String message) throws TelegramApiException {
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(sendMessageCaptor.capture());
        Assertions.assertThat(sendMessageCaptor.getValue())
                .extracting(
                        "chatId",
                        "text"
                )
                .containsExactly(
                        "456",
                        message
                );
    }
}