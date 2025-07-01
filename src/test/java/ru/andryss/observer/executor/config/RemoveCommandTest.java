package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class RemoveCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testWithoutArguments() {
        sendCommand("remove");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testWithUnknownConfigKey() {
        sendCommand("remove", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testOnlyConfigKey() {
        sendCommand("remove", "DISCLAIMER_TEXT");

        verifyMessageSent("ERROR: No value to remove argument");
    }

    @Test
    @SneakyThrows
    void testWithNotLongValue() {
        sendCommand("remove", "DISCLAIMER_TEXT", "123", "abc");

        verifyMessageSent("ERROR: For input string: \"abc\"");
    }

    @Test
    @SneakyThrows
    void testNotLongListConfigKey() {
        sendCommand("remove", "DISCLAIMER_TEXT", "123", "456", "789");

        verifyMessageSent("ERROR: DISCLAIMER_TEXT is not long list");
    }

    @Test
    @SneakyThrows
    void testRemoveLongValues() {
        sendCommand("add", "ADMIN_USER_IDS", "123", "456", "789");

        verifyMessageSent("OK");

        sendCommand("remove", "ADMIN_USER_IDS", "123", "-1");

        verifyMessageSent(2, "OK");

        sendCommand("get", "ADMIN_USER_IDS");

        verifyMessageSent(3, """
                ```json
                [456,789]
                ```
                """);
    }

}