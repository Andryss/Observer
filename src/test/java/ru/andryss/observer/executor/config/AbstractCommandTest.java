package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
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

import static org.mockito.Mockito.times;

class AbstractCommandTest extends BaseDbTest {

    @Autowired
    ConfigCommandExecutor executor;

    @MockitoBean
    AbsSender sender;

    @SneakyThrows
    void sendCommand(String... arguments) {
        Chat chat = new Chat();
        chat.setId(456L);
        Message message = new Message();
        message.setChat(chat);
        message.setText("/config " + String.join(" ", arguments));
        Update update = new Update();
        update.setMessage(message);

        executor.process(update, sender);
    }

    @SneakyThrows
    void verifyMessageSent(String message) {
        verifyMessageSent(1, message);
    }

    @SneakyThrows
    void verifyMessageSent(int count, String message) {
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender, times(count)).execute(sendMessageCaptor.capture());
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