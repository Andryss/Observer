package ru.andryss.observer.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.KeyStorageService;

@Component
@RequiredArgsConstructor
public class GetUserIdCommandExecutor extends AbstractCommandExecutor {

    private final KeyStorageService keyStorageService;

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/userid");

    @Override
    public boolean isActive() {
        return keyStorageService.get("getUserIdCommandExecutor.active", false);
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        Message message = update.getMessage();
        User user = message.getFrom();
        String text = "%s: %s".formatted(user.getUserName(), user.getId());
        sender.execute(new SendMessage(message.getChatId().toString(), text));
    }
}
