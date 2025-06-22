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

class GetUserIdCommandExecutorTest extends BaseDbTest {

    @Autowired
    GetUserIdCommandExecutor executor;

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
        keyStorageService.put("getUserIdCommandExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessCommandWithoutMention() {
        Message message = new Message();
        message.setText("prefix /userid suffix");
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
        message.setText("prefix /userid@bot suffix");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 7, 11)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcess() {
        User user = new User();
        user.setId(567L);
        user.setUserName("some-user");
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setFrom(user);
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        executor.process(update, sender);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting("chatId", "text")
                .containsExactly("123", "some-user: 567");
        Mockito.verifyNoMoreInteractions(sender);
    }

}