package ru.andryss.observer.model;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of all app configurations. Each configuration consist of:
 * - string key with which value placed in app database
 * - configuration value type ({@link TypeReference})
 * - default value in JSON format
 */
@Getter
@RequiredArgsConstructor
public enum ConfigKey {
    // Always check that defaultValue type is equal to TypeReference generic type
    DELETE_BLACKLIST_MESSAGES_EXECUTOR_ACTIVE(
            "deleteBlacklistMessagesExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    BLACKLIST_USER_IDS(
            "blacklist.userIds",
            List.<Long>of(), new TypeReference<List<Long>>() {}
    ),
    DISCLAIMER_COMMAND_EXECUTOR_ACTIVE(
            "disclaimerCommandExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    @SuppressWarnings("checkstyle:LineLength")
    DISCLAIMER_TEXT(
            "disclaimer.text",
            """
            Я никоим образом НЕ стремлюсь кого-либо оскорбить, задеть или унизить. Мои высказывания — это исключительно выражение моего личного субъективного мнения, которое НЕ претендует на истину в последней инстанции и НЕ направлено против каких-либо людей, групп или сообществ.
            Я искренне уважаю труд всех людей — независимо от национальности, цвета кожи, вероисповедания, пола, гендерной идентичности, сексуальной ориентации, возраста, внешности, социального статуса или места проживания. Я глубоко убеждён, что каждый человек и его деятельность важны для функционирования и развития общества. Любой труд — физический или умственный, творческий или технический — достоин признания и уважения.
            Я также понимаю, что у каждого — свой путь, опыт, культура и ценности, и именно в этом заключается богатство и сложность человеческого общества. Я НЕ считаю себя вправе судить или обесценивать чужой взгляд, а лишь делюсь собственным, сформированным под влиянием личного опыта, окружения и восприятия мира.
            Прошу воспринимать мои слова НЕ как категоричные утверждения, а как открытый и уважительный взгляд на происходящее с моей позиции. Если мои формулировки вызывают непонимание или кажутся резкими — прошу прощения и открыт к диалогу.
            """, new TypeReference<String>() {}
    ),
    GET_CHAT_ID_COMMAND_EXECUTOR_ACTIVE(
            "getChatIdCommandExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    GET_USER_ID_COMMAND_EXECUTOR_ACTIVE(
            "getUserIdCommandExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    SEND_MESSAGE_EXECUTOR_ACTIVE(
            "sendMessageExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    SEND_MESSAGE_ALLOWED_CHATS(
            "sendMessageExecutor.allowedChats",
            List.<Long>of(), new TypeReference<List<Long>>() {}
    ),
    CONFIG_COMMAND_EXECUTOR_ACTIVE(
            "configCommandExecutor.active",
            false, new TypeReference<Boolean>() {}
    ),
    ADMIN_USER_IDS(
            "admin.userIds",
            List.<Long>of(), new TypeReference<List<Long>>() {}
    );

    private final String key;
    private final Object defaultValue;
    private final TypeReference<?> type;
}
