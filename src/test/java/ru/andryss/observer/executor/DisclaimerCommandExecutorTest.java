package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.KeyStorageService;

class DisclaimerCommandExecutorTest extends BaseDbTest {

    @Autowired
    DisclaimerCommandExecutor executor;

    @Autowired
    KeyStorageService keyStorageService;

    @MockitoBean
    AbsSender sender;

    @Test
    void testIsActiveDefault() {
        Assertions.assertThat(executor.isActive()).isFalse();
    }

    @Test
    void testIsActiveKeySet() {
        keyStorageService.put("disclaimerCommandExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessCommandWithoutMention() {
        Message message = new Message();
        message.setText("prefix /disclaimer suffix");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 7, 11)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    void testCanProcessCommandWithMention() {
        Message message = new Message();
        message.setText("prefix /disclaimer@bot suffix");
        message.setEntities(List.of(
                new MessageEntity("bot_command", 7, 15)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessDefaultDisclaimer() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        executor.process(update, sender);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting("chatId", "text")
                .containsExactly("123", """
                        Я никоим образом НЕ стремлюсь кого-либо оскорбить, задеть или унизить. Мои высказывания — это исключительно выражение моего личного субъективного мнения, которое НЕ претендует на истину в последней инстанции и НЕ направлено против каких-либо людей, групп или сообществ.
                        Я искренне уважаю труд всех людей — независимо от национальности, цвета кожи, вероисповедания, пола, гендерной идентичности, сексуальной ориентации, возраста, внешности, социального статуса или места проживания. Я глубоко убеждён, что каждый человек и его деятельность важны для функционирования и развития общества. Любой труд — физический или умственный, творческий или технический — достоин признания и уважения.
                        Я также понимаю, что у каждого — свой путь, опыт, культура и ценности, и именно в этом заключается богатство и сложность человеческого общества. Я НЕ считаю себя вправе судить или обесценивать чужой взгляд, а лишь делюсь собственным, сформированным под влиянием личного опыта, окружения и восприятия мира.
                        Прошу воспринимать мои слова НЕ как категоричные утверждения, а как открытый и уважительный взгляд на происходящее с моей позиции. Если мои формулировки вызывают непонимание или кажутся резкими — прошу прощения и открыт к диалогу.
                        """);
        Mockito.verifyNoMoreInteractions(sender);
    }

    @Test
    @SneakyThrows
    void testProcessDisclaimerSet() {
        Chat chat = new Chat();
        chat.setId(123L);
        Message message = new Message();
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        keyStorageService.put("disclaimer.text", "hah");

        executor.process(update, sender);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .extracting("chatId", "text")
                .containsExactly("123", "hah");
        Mockito.verifyNoMoreInteractions(sender);
    }

}