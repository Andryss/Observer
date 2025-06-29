package ru.andryss.observer.executor.config;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class GetCommandTest extends AbstractCommandTest {

    @Test
    @SneakyThrows
    void testGetWithoutArguments() {
        sendCommand("get");

        verifyMessageSent("ERROR: No key argument");
    }

    @Test
    @SneakyThrows
    void testGetWithUnknownConfigKey() {
        sendCommand("get", "absent_key");

        verifyMessageSent("ERROR: No enum constant ru.andryss.observer.model.ConfigKey.absent_key");
    }

    @Test
    @SneakyThrows
    void testGetBooleanValue() {
        sendCommand("get", "CONFIG_COMMAND_EXECUTOR_ACTIVE");

        verifyMessageSent("""
                ```json
                false
                ```
                """);
    }

    @Test
    @SneakyThrows
    void testGetLongListValue() {
        sendCommand("get", "ADMIN_USER_IDS");

        verifyMessageSent("""
                ```json
                []
                ```
                """);
    }

    @Test
    @SneakyThrows
    void testGetStringValue() {
        sendCommand("get", "DISCLAIMER_TEXT");

        verifyMessageSent("""
                ```json
                "Я никоим образом НЕ стремлюсь кого-либо оскорбить, задеть или унизить. Мои высказывания — это исключительно выражение моего личного субъективного мнения, которое НЕ претендует на истину в последней инстанции и НЕ направлено против каких-либо людей, групп или сообществ.\\nЯ искренне уважаю труд всех людей — независимо от национальности, цвета кожи, вероисповедания, пола, гендерной идентичности, сексуальной ориентации, возраста, внешности, социального статуса или места проживания. Я глубоко убеждён, что каждый человек и его деятельность важны для функционирования и развития общества. Любой труд — физический или умственный, творческий или технический — достоин признания и уважения.\\nЯ также понимаю, что у каждого — свой путь, опыт, культура и ценности, и именно в этом заключается богатство и сложность человеческого общества. Я НЕ считаю себя вправе судить или обесценивать чужой взгляд, а лишь делюсь собственным, сформированным под влиянием личного опыта, окружения и восприятия мира.\\nПрошу воспринимать мои слова НЕ как категоричные утверждения, а как открытый и уважительный взгляд на происходящее с моей позиции. Если мои формулировки вызывают непонимание или кажутся резкими — прошу прощения и открыт к диалогу.\\n"
                ```
                """);
    }

}