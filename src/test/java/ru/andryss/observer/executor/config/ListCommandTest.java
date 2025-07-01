package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class ListCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testListCommand() {
        sendCommand("list");

        verifyMessageSent("""
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
                        `CLEAR_CONTEXT_COMMAND_EXECUTOR_ACTIVE` -- (java.lang.Boolean)
                        `MODEL_INSTRUCTION` -- (java.lang.String)
                        """);
    }

}