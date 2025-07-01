package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class SetstrCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testWithoutArguments() {
        sendCommand("setstr");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testWithUnknownConfigKey() {
        sendCommand("setstr", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testOnlyConfigKey() {
        sendCommand("setstr", "DISCLAIMER_TEXT");

        verifyMessageSent("ERROR: No value to set argument");
    }

    @Test
    @SneakyThrows
    void testNotStringConfigKey() {
        sendCommand("setstr", "ADMIN_USER_IDS", "123", "abc");

        verifyMessageSent("ERROR: ADMIN_USER_IDS is not string");
    }

    @Test
    @SneakyThrows
    void testSetStringValue() {
        sendCommand("setstr", "DISCLAIMER_TEXT", "123", "abc", "def");

        verifyMessageSent("OK");

        sendCommand("get", "DISCLAIMER_TEXT");

        verifyMessageSent(2, """
                ```json
                "123 abc def"
                ```
                """);
    }

}