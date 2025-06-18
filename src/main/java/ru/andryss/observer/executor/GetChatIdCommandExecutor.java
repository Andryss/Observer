package ru.andryss.observer.executor;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class GetChatIdCommandExecutor implements UpdateExecutor {

    private final KeyStorageService keyStorageService;

    @Override
    public boolean isActive() {
        return keyStorageService.get("getChatIdCommandExecutor.active", false);
    }

    @Override
    public boolean canProcess(Update update) {
        return update.hasMessage() &&
                update.getMessage().hasEntities() &&
                update.getMessage().getEntities().stream()
                        .filter(entity -> Objects.equals("bot_command", entity.getType()))
                        .map(this::extractCommand)
                        .anyMatch("chatid"::equals);
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        String chatId = update.getMessage().getChatId().toString();
        sender.execute(new SendMessage(chatId, chatId));
    }

    private String extractCommand(MessageEntity entity) {
        String text = entity.getText();
        int tagIndex = text.indexOf('@');
        return text.substring(1, tagIndex == -1 ? text.length() : tagIndex);
    }
}
