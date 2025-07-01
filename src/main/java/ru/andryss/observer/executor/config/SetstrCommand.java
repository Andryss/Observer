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
public class SetstrCommand implements ConfigCommand {

    private final ConfigService configService;

    @Getter
    private final ConfigCommandInfo configCommandInfo = new ConfigCommandInfo("setstr");

    @Override
    public void execute(List<String> arguments, Update update, AbsSender sender) throws Exception {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("No key argument");
        }

        ConfigKey configKey = ConfigKey.valueOf(arguments.get(0));

        if (arguments.size() < 2) {
            throw new IllegalArgumentException("No value to set argument");
        }

        StringBuilder builder = new StringBuilder(arguments.get(1));
        for (int idx = 2; idx < arguments.size(); idx++) {
            builder.append(' ').append(arguments.get(idx));
        }

        configService.putString(configKey, builder.toString());
        sender.execute(new SendMessage(update.getMessage().getChatId().toString(), "OK"));
    }
}
