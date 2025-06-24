package ru.andryss.observer.executor;

import java.util.Objects;

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
import ru.andryss.observer.service.ConfigService;
import ru.andryss.observer.service.GptModelService;

import static ru.andryss.observer.model.ConfigKey.SEND_MESSAGE_ALLOWED_CHATS;
import static ru.andryss.observer.model.ConfigKey.SEND_MESSAGE_EXECUTOR_ACTIVE;

@Component
@RequiredArgsConstructor
public class SendMessageExecutor implements UpdateExecutor {

    private final ConfigService configService;
    private final GptModelService gptModelService;
    private final BotUserInfoProviderService butUserProvider;


    @Override
    public boolean isActive() {
        return configService.getBoolean(SEND_MESSAGE_EXECUTOR_ACTIVE);
    }

    @Override
    public boolean canProcess(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasText() &&
                needToAnswerToMessage(update) &&
                configService.getLongList(SEND_MESSAGE_ALLOWED_CHATS).contains(update.getMessage().getChatId());
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

    /**
     * Answers to:
     * - all messages in private chats
     * - in-group messages that replied to bot messages
     * - in-group messages with bot mention
     */
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
