package ru.andryss.observer.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class GetChatIdCommandExecutor extends AbstractCommandExecutor {

    private final KeyStorageService keyStorageService;

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/chatid");

    @Override
    public boolean isActive() {
        return keyStorageService.get("getChatIdCommandExecutor.active", false);
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        String chatId = update.getMessage().getChatId().toString();
        sender.execute(new SendMessage(chatId, chatId));
    }
}
