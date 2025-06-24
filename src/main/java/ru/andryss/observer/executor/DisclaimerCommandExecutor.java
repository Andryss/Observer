package ru.andryss.observer.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.ConfigService;

import static ru.andryss.observer.model.ConfigKey.DISCLAIMER_COMMAND_EXECUTOR_ACTIVE;
import static ru.andryss.observer.model.ConfigKey.DISCLAIMER_TEXT;

@Component
@RequiredArgsConstructor
public class DisclaimerCommandExecutor extends AbstractCommandExecutor {

    private final ConfigService configService;

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/disclaimer");

    @Override
    public boolean isActive() {
        return configService.getBoolean(DISCLAIMER_COMMAND_EXECUTOR_ACTIVE);
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        String disclaimer = configService.getString(DISCLAIMER_TEXT);
        sender.execute(new SendMessage(update.getMessage().getChatId().toString(), disclaimer));
    }
}
