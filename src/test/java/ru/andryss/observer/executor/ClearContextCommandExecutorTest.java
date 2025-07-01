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
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class ClearContextCommandExecutorTest extends BaseDbTest {

    @Autowired
    ClearContextCommandExecutor executor;

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
        keyStorageService.put("clearContextCommandExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessEmptyAdmins() {
        User user = new User();
        user.setId(123L);
        Message message = new Message();
        message.setFrom(user);
        message.setText("/clearcontext");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 13)
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
        message.setText("/clearcontext");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 13)
        ));
        Update update = new Update();
        update.setMessage(message);

        keyStorageService.put("admin.userIds", List.of(123L));

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessDefaultDisclaimer() {
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
                .containsExactly("123", "Чистый, как совесть младенца");
        Mockito.verifyNoMoreInteractions(sender);
    }

}