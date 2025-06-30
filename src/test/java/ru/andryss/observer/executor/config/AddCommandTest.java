package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class AddCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testWithoutArguments() {
        sendCommand("add");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testWithUnknownConfigKey() {
        sendCommand("add", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testOnlyConfigKey() {
        sendCommand("add", "DISCLAIMER_TEXT");

        verifyMessageSent("ERROR: No value to add argument");
    }

    @Test
    @SneakyThrows
    void testWithNotLongValue() {
        sendCommand("add", "DISCLAIMER_TEXT", "123", "abc");

        verifyMessageSent("ERROR: For input string: \"abc\"");
    }

    @Test
    @SneakyThrows
    void testNotLongListConfigKey() {
        sendCommand("add", "DISCLAIMER_TEXT", "123", "456", "789");

        verifyMessageSent("ERROR: DISCLAIMER_TEXT is not long list");
    }

    @Test
    @SneakyThrows
    void testAddLongValues() {
        sendCommand("add", "ADMIN_USER_IDS", "123", "456", "789");

        verifyMessageSent("OK");

        sendCommand("get", "ADMIN_USER_IDS");

        verifyMessageSent(2, """
                ```json
                [123,456,789]
                ```
                """);
    }

    @Test
    @SneakyThrows
    void testSkipExistingLongValues() {
        sendCommand("add", "ADMIN_USER_IDS", "123", "456", "789");

        verifyMessageSent("OK");

        sendCommand("add", "ADMIN_USER_IDS", "789", "-1");

        verifyMessageSent(2, "OK");

        sendCommand("get", "ADMIN_USER_IDS");

        verifyMessageSent(3, """
                ```json
                [123,456,789,-1]
                ```
                """);
    }

}