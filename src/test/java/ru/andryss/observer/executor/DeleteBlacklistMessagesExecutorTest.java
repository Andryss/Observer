package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class DeleteBlacklistMessagesExecutorTest extends BaseDbTest {

    @Autowired
    DeleteBlacklistMessagesExecutor executor;

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
        keyStorageService.put("deleteBlacklistMessagesExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessNoMessage() {
        Update update = new Update();

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessHasMessage() {
        Message message = new Message();
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessEmptyBlacklist() {
        executor.process(buildUpdate(), sender);

        Mockito.verifyNoMoreInteractions(sender);
    }

    @Test
    @SneakyThrows
    void testProcessNotInBlacklist() {
        keyStorageService.put("deleteBlacklistMessagesExecutor.blacklist", List.of(111L));

        executor.process(buildUpdate(), sender);

        Mockito.verifyNoMoreInteractions(sender);
    }

    @Test
    @SneakyThrows
    void testProcessUserInBlacklist() {
        keyStorageService.put("blacklist.userIds", List.of(123L));

        executor.process(buildUpdate(), sender);

        ArgumentCaptor<DeleteMessage> argumentCaptor = ArgumentCaptor.forClass(DeleteMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting("chatId", "messageId")
                .containsExactly("456", 555);
        Mockito.verifyNoMoreInteractions(sender);
    }

    private static Update buildUpdate() {
        User user = new User();
        user.setId(123L);

        Chat chat = new Chat();
        chat.setId(456L);

        Message message = new Message();
        message.setMessageId(555);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }
}