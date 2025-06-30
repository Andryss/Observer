package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class DisableCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testDisableWithoutArguments() {
        sendCommand("disable");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testDisableWithUnknownConfigKey() {
        sendCommand("disable", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testDisableBooleanValue() {
        sendCommand("disable", "CONFIG_COMMAND_EXECUTOR_ACTIVE");

        verifyMessageSent("OK");
    }

    @Test
    @SneakyThrows
    void testDisableNotBooleanValue() {
        sendCommand("disable", "ADMIN_USER_IDS");

        verifyMessageSent("ERROR: ADMIN_USER_IDS is not boolean");
    }

}