package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class EnableCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testEnableWithoutArguments() {
        sendCommand("enable");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testEnableWithUnknownConfigKey() {
        sendCommand("enable", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testEnableBooleanValue() {
        sendCommand("enable", "CONFIG_COMMAND_EXECUTOR_ACTIVE");

        verifyMessageSent("OK");
    }

    @Test
    @SneakyThrows
    void testEnableNotBooleanValue() {
        sendCommand("enable", "ADMIN_USER_IDS");

        verifyMessageSent("ERROR: ADMIN_USER_IDS is not boolean");
    }

}