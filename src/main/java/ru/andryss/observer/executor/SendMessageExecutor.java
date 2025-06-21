package ru.andryss.observer.executor;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.MessageDto;
import ru.andryss.observer.service.BotUserInfoProviderService;
import ru.andryss.observer.service.GptModelService;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class SendMessageExecutor implements UpdateExecutor {

    private final KeyStorageService keyStorageService;
    private final GptModelService gptModelService;
    private final BotUserInfoProviderService butUserProvider;


    @Override
    public boolean isActive() {
        return keyStorageService.get("sendMessageExecutor.active", false);
    }

    @Override
    public boolean canProcess(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                needToAnswerToMessage(update) &&
                keyStorageService.<List<Long>>get("sendMessageExecutor.allowedChats", List.of(),
                        new TypeReference<>() {}).contains(update.getMessage().getChatId());
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();

        // Send typing action to show user that something happens
        SendChatAction action = new SendChatAction();
        action.setChatId(chatId);
        action.setAction(ActionType.TYPING);
        sender.execute(action);

        MessageDto userMessage = new MessageDto(text);
        MessageDto responseMessage = gptModelService.handleMessage(chatId.toString(), userMessage);

        SendMessage sendMessage = new SendMessage(chatId.toString(), responseMessage.getText());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setAllowSendingWithoutReply(true);
        sender.execute(sendMessage);
    }

    private boolean needToAnswerToMessage(Update update) {
        return Objects.equals(update.getMessage().getChat().getType(), "private") ||
                (Objects.equals(update.getMessage().getChat().getType(), "supergroup") &&
                        (update.getMessage().getReplyToMessage() != null &&
                                Objects.equals(update.getMessage().getReplyToMessage().getFrom().getId(),
                                        butUserProvider.getBotUser().getId())
                        ) ||
                        (update.getMessage().hasEntities() &&
                                update.getMessage().getEntities().stream()
                                        .filter(entity -> Objects.equals("mention", entity.getType()))
                                        .map(this::extractMention)
                                        .anyMatch(mention -> butUserProvider.getBotUser().getUserName().equals(mention))

                        )
                );
    }

    private String extractMention(MessageEntity entity) {
        return entity.getText().substring(1);
    }
}
