package ru.andryss.observer;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@SpringBootTest
class UpdateDispatcherTest {

    @Autowired
    UpdateDispatcher dispatcher;

    @MockitoBean
    ExecutorService updateExecutors;

    @BeforeEach
    void before() {
        Mockito.reset(updateExecutors);
    }

    @Test
    void testEmptyUpdate() {
        Update emptyUpdate = new Update();

        dispatcher.onUpdateReceived(emptyUpdate);

        Mockito.verify(updateExecutors).submit(Mockito.<Runnable>any());
        Mockito.verifyNoMoreInteractions(updateExecutors);
    }

    @Test
    void testNewMessageUpdate() {
        Message message = new Message();
        message.setDate((int) (Instant.now().toEpochMilli() / 1000));

        Update update = new Update();
        update.setMessage(message);

        dispatcher.onUpdateReceived(update);

        Mockito.verify(updateExecutors).submit(Mockito.<Runnable>any());
        Mockito.verifyNoMoreInteractions(updateExecutors);
    }

    @Test
    void testOldMessageUpdate() {
        Message message = new Message();
        message.setDate((int) (Instant.now().toEpochMilli() / 1000 - 60));

        Update update = new Update();
        update.setMessage(message);

        dispatcher.onUpdateReceived(update);

        Mockito.verifyNoMoreInteractions(updateExecutors);
    }

}