package ru.andryss.observer.executor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class DeleteBlacklistMessagesExecutor implements UpdateExecutor {

    private final KeyStorageService keyStorageService;

    @Override
    public boolean isActive() {
        return keyStorageService.get("deleteBlacklistMessagesExecutor.active", false);
    }

    @Override
    public boolean canProcess(Update update) {
        return update.hasMessage();
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        Message message = update.getMessage();
        Long senderId = message.getFrom().getId();

        // TODO: fix List<Long> parsing
        List<Integer> blacklist = keyStorageService.get("deleteBlacklistMessagesExecutor.blacklist", List.of());

        if (blacklist.contains(senderId.intValue())) {
            Long chatId = message.getChatId();
            Integer messageId = message.getMessageId();
            sender.execute(new DeleteMessage(chatId.toString(), messageId));
        }
    }
}
