package ru.andryss.observer.executor;

import java.util.List;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.model.Alternative;
import ru.andryss.observer.generated.yandexgpt.model.CompletionOptions;
import ru.andryss.observer.generated.yandexgpt.model.CompletionRequest;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponse;
import ru.andryss.observer.generated.yandexgpt.model.CompletionResponseResult;
import ru.andryss.observer.generated.yandexgpt.model.MessageRole;
import ru.andryss.observer.generated.yandexgpt.model.ReasoningOptions;
import ru.andryss.observer.service.KeyStorageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class SendMessageExecutorTest extends BaseDbTest {

    @Autowired
    SendMessageExecutor executor;

    @Autowired
    KeyStorageService keyStorageService;

    @Autowired
    YandexGptApi yandexGptApi;

    @MockitoBean
    AbsSender sender;

    @BeforeEach
    @SneakyThrows
    void before() {
        Mockito.clearInvocations(yandexGptApi);

        User user = new User();
        user.setId(333L);
        user.setUserName("observer_bot");
        Mockito.when(sender.execute(Mockito.<GetMe>any()))
                .thenReturn(user);
    }

    @Test
    void testIsActiveDefault() {
        Assertions.assertThat(executor.isActive()).isFalse();
    }

    @Test
    void testIsActiveKeySet() {
        keyStorageService.put("sendMessageExecutor.active", true);

        Assertions.assertThat(executor.isActive()).isTrue();
    }

    @Test
    void testCanProcessNoMessage() {
        Update update = new Update();

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessNoMessageText() {
        Message message = new Message();
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessSupergroupMessage() {
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("some-text");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessNonSpecialGroupMessage() {
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("some-text");
        message.setChat(chat);
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    @SneakyThrows
    void testCanProcessGroupMessageRepliedToNonBot() {
        User replyUser = new User();
        replyUser.setId(-333L);
        Message replyMessage = new Message();
        replyMessage.setFrom(replyUser);
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("some-text");
        message.setChat(chat);
        message.setReplyToMessage(replyMessage);
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessGroupMessageHasNoMention() {
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("/commandd #hashtag $USD https://url.com hi!");
        message.setChat(chat);
        message.setEntities(List.of(
                new MessageEntity("bot_command", 0, 9),
                new MessageEntity("hashtag", 10, 8),
                new MessageEntity("cashtag", 19, 4),
                new MessageEntity("url", 24, 15)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    @SneakyThrows
    void testCanProcessGroupMessageHasAnotherUserMention() {
        Chat chat = new Chat();
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("@observer_bott");
        message.setChat(chat);
        message.setEntities(List.of(
                new MessageEntity("mention", 0, 14)
        ));
        Update update = new Update();
        update.setMessage(message);

        Assertions.assertThat(executor.canProcess(update)).isFalse();
    }

    @Test
    void testCanProcessAllowedChatsIsEmpty() {
        Assertions.assertThat(executor.canProcess(buildUpdate(""))).isFalse();
    }

    @Test
    void testCanProcessChatNotInAllowedChats() {
        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(456L));

        Assertions.assertThat(executor.canProcess(buildUpdate("not-empty"))).isFalse();
    }

    @Test
    void testCanProcessPrivateMessage() {
        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(123L));

        Assertions.assertThat(executor.canProcess(buildUpdate("not-empty"))).isTrue();
    }

    @Test
    @SneakyThrows
    void testCanProcessGroupMessageRepliedToBot() {
        User replyUser = new User();
        replyUser.setId(333L);
        Message replyMessage = new Message();
        replyMessage.setFrom(replyUser);
        Chat chat = new Chat();
        chat.setId(123L);
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("some-text");
        message.setChat(chat);
        message.setReplyToMessage(replyMessage);
        Update update = new Update();
        update.setMessage(message);

        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(123L));

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testCanProcessGroupMessageHasBotMention() {
        Chat chat = new Chat();
        chat.setId(123L);
        chat.setType("supergroup");
        Message message = new Message();
        message.setText("@observer_bot");
        message.setChat(chat);
        message.setEntities(List.of(
                new MessageEntity("mention", 0, 13)
        ));
        Update update = new Update();
        update.setMessage(message);

        keyStorageService.put("sendMessageExecutor.allowedChats", List.of(123L));

        Assertions.assertThat(executor.canProcess(update)).isTrue();
    }

    @Test
    @SneakyThrows
    void testProcessMessageSent() {
        mockYandexGptApiResponse();

        executor.process(buildUpdate("some-text"), sender);

        verifyYandexGptApiRequest(1,
                systemInstructionMessage(),
                userMessage("some-text")
        );
        verifyTypingEventSent(1);
        verifyMessageSent(1);

        verifyNoMoreMocksInteractions();
    }

    @Test
    @SneakyThrows
    void testProcessWithContextSaved() {
        mockYandexGptApiResponse();

        executor.process(buildUpdate("some-text-1"), sender);
        executor.process(buildUpdate("some-text-2"), sender);

        verifyYandexGptApiRequest(2,
                systemInstructionMessage(),
                userMessage("some-text-1"),
                assistantResponse(),
                userMessage("some-text-2")
        );
        verifyTypingEventSent(2);
        verifyMessageSent(2);

        verifyNoMoreMocksInteractions();
    }

    @Test
    @SneakyThrows
    void testProcessWithOldContextCleared() {
        mockYandexGptApiResponse();

        executor.process(buildUpdate("some-text-1"), sender);
        executor.process(buildUpdate("some-text-2"), sender);
        executor.process(buildUpdate("some-text-3"), sender);
        executor.process(buildUpdate("some-text-4"), sender);

        verifyYandexGptApiRequest(4,
                systemInstructionMessage(),
                userMessage("some-text-2"),
                assistantResponse(),
                userMessage("some-text-3"),
                assistantResponse(),
                userMessage("some-text-4")
        );
        verifyTypingEventSent(4);
        verifyMessageSent(4);

        verifyNoMoreMocksInteractions();
    }

    private static Update buildUpdate(String text) {
        Chat chat = new Chat();
        chat.setId(123L);
        chat.setType("private");

        Message message = new Message();
        message.setMessageId(456);
        message.setText(text);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private void mockYandexGptApiResponse() {
        Mockito.when(yandexGptApi.foundationModelsV1CompletionPost(any()))
                .thenReturn(new CompletionResponse()
                        .result(new CompletionResponseResult()
                                .addAlternativesItem(new Alternative()
                                        .message(new ru.andryss.observer.generated.yandexgpt.model.Message()
                                                .role(MessageRole.ASSISTANT)
                                                .text("some response")
                                        )
                                        .status(Alternative.StatusEnum.FINAL)
                                )
                        )
                );
    }

    private void verifyYandexGptApiRequest(int count, ru.andryss.observer.generated.yandexgpt.model.Message... messages) {
        Mockito.verify(yandexGptApi, times(count)).foundationModelsV1CompletionPost(any());
        Mockito.verify(yandexGptApi).foundationModelsV1CompletionPost(new CompletionRequest()
                .modelUri("mock-model-uri")
                .completionOptions(new CompletionOptions()
                        .stream(false)
                        .temperature(0.111)
                        .maxTokens("2")
                        .reasoningOptions(new ReasoningOptions()
                                .mode(ReasoningOptions.ModeEnum.DISABLED)
                        )
                )
                .messages(List.of(messages))
        );
    }

    private void verifyTypingEventSent(int count) throws TelegramApiException {
        ArgumentCaptor<SendChatAction> sendChatActionCaptor = ArgumentCaptor.forClass(SendChatAction.class);
        Mockito.verify(sender, times(count)).execute(sendChatActionCaptor.capture());
        Assertions.assertThat(sendChatActionCaptor.getValue())
                .extracting(
                        "chatId",
                        "action"
                )
                .containsExactly(
                        "123",
                        "typing"
                );
    }

    private void verifyMessageSent(int count) throws TelegramApiException {
        ArgumentCaptor<SendMessage> sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(sender, times(count)).execute(sendMessageCaptor.capture());
        Assertions.assertThat(sendMessageCaptor.getValue())
                .extracting(
                        "chatId",
                        "text",
                        "replyToMessageId",
                        "allowSendingWithoutReply"
                )
                .containsExactly(
                        "123",
                        "some response",
                        456,
                        true
                );
    }

    private void verifyNoMoreMocksInteractions() {
        Mockito.verifyNoMoreInteractions(yandexGptApi);
        Mockito.verifyNoMoreInteractions(sender);
    }

    private static ru.andryss.observer.generated.yandexgpt.model.Message systemInstructionMessage() {
        return message(MessageRole.SYSTEM, "mock-model-instruction");
    }

    private static ru.andryss.observer.generated.yandexgpt.model.Message userMessage(String text) {
        return message(MessageRole.USER, text);
    }

    private static ru.andryss.observer.generated.yandexgpt.model.Message assistantResponse() {
        return message(MessageRole.ASSISTANT, "some response");
    }

    private static ru.andryss.observer.generated.yandexgpt.model.Message message(MessageRole user, String text) {
        return new ru.andryss.observer.generated.yandexgpt.model.Message().role(user).text(text);
    }
}