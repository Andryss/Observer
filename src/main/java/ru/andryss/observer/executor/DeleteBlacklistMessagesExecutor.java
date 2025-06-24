package ru.andryss.observer.executor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.ConfigKey;
import ru.andryss.observer.service.ConfigService;

import static ru.andryss.observer.model.ConfigKey.BLACKLIST_USER_IDS;

@Component
@RequiredArgsConstructor
public class DeleteBlacklistMessagesExecutor implements UpdateExecutor {

    private final ConfigService configService;

    @Override
    public boolean isActive() {
        return configService.getBoolean(ConfigKey.DELETE_BLACKLIST_MESSAGES_EXECUTOR_ACTIVE);
    }

    @Override
    public boolean canProcess(Update update) {
        return update.hasMessage();
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        Message message = update.getMessage();
        Long senderId = message.getFrom().getId();

        List<Long> blacklist = configService.getLongList(BLACKLIST_USER_IDS);

        if (blacklist.contains(senderId)) {
            Long chatId = message.getChatId();
            Integer messageId = message.getMessageId();
            sender.execute(new DeleteMessage(chatId.toString(), messageId));
        }
    }
}
