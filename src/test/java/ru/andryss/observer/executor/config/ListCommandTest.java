package ru.andryss.observer.executor.config;

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

class ListCommandTest extends BaseDbTest {

    @Autowired
    ConfigCommandExecutor executor;

    @MockitoBean
    AbsSender sender;

    @Test
    @SneakyThrows
    void testListCommand() {
        Chat chat = new Chat();
        chat.setId(456L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/config list");
        Update update = new Update();
        update.setMessage(message);

        executor.process(update, sender);

        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(sendMessageCaptor.capture());
        Assertions.assertThat(sendMessageCaptor.getValue())
                .extracting(
                        "chatId",
                        "text"
                )
                .containsExactly(
                        "456",
                        """
                        `DELETE_BLACKLIST_MESSAGES_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `BLACKLIST_USER_IDS` -- (java.util.List<java.lang.Long>)
                        `DISCLAIMER_COMMAND_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `DISCLAIMER_TEXT` -- (java.lang.String)
                        `GET_CHAT_ID_COMMAND_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `GET_USER_ID_COMMAND_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `SEND_MESSAGE_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `SEND_MESSAGE_ALLOWED_CHATS` -- (java.util.List<java.lang.Long>)
                        `CONFIG_COMMAND_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `ADMIN_USER_IDS` -- (java.util.List<java.lang.Long>)
                        """
                );
    }

}