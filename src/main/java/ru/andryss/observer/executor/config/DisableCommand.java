package ru.andryss.observer.executor.config;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.ConfigKey;
import ru.andryss.observer.service.ConfigService;

@Component
@RequiredArgsConstructor
public class DisableCommand implements ConfigCommand {

    private final ConfigService configService;

    @Getter
    private final ConfigCommandInfo configCommandInfo = new ConfigCommandInfo("disable");

    @Override
    public void execute(List<String> arguments, Update update, AbsSender sender) throws Exception {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("No key argument");
        }

        ConfigKey configKey = ConfigKey.valueOf(arguments.get(0));

        configService.putBoolean(configKey, false);
        sender.execute(new SendMessage(update.getMessage().getChatId().toString(), "OK"));
    }
}
