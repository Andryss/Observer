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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.MessageDto;
import ru.andryss.observer.service.GptModelService;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class SendMessageExecutor implements UpdateExecutor {

    private final KeyStorageService keyStorageService;
    private final GptModelService gptModelService;


    @Override
    public boolean isActive() {
        return keyStorageService.get("sendMessageExecutor.active", false);
    }

    @Override
    public boolean canProcess(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        if (!message.hasText()) {
            return false;
        }
        if (!Objects.equals(message.getChat().getType(), "private")){
            return false;
        }
        Long chatId = message.getChatId();
        List<Long> allowedChats = keyStorageService.get("sendMessageExecutor.allowedChats", List.of(),
                new TypeReference<>() {});
        return allowedChats.contains(chatId);
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
}
