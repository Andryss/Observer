package ru.andryss.observer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.objects.Update;

@SpringBootTest
class UpdateDispatcherTest {

    @Autowired
    UpdateDispatcher dispatcher;

    @Test
    void testSimpleUpdate() {
        Update emptyUpdate = new Update();
        dispatcher.onUpdateReceived(emptyUpdate);
    }

}