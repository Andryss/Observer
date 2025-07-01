package ru.andryss.observer.executor.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.ConfigKey;
import ru.andryss.observer.service.ConfigService;

@Component
@RequiredArgsConstructor
public class RemoveCommand implements ConfigCommand {

    private final TransactionTemplate transactionTemplate;
    private final ConfigService configService;

    @Getter
    private final ConfigCommandInfo configCommandInfo = new ConfigCommandInfo("remove");

    @Override
    public void execute(List<String> arguments, Update update, AbsSender sender) throws Exception {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("No key argument");
        }

        ConfigKey configKey = ConfigKey.valueOf(arguments.get(0));

        if (arguments.size() < 2) {
            throw new IllegalArgumentException("No value to remove argument");
        }

        List<Long> argumentsParsed = new ArrayList<>(arguments.size() - 1);
        for (int idx = 1; idx < arguments.size(); idx++) {
            argumentsParsed.add(Long.parseLong(arguments.get(idx)));
        }

        transactionTemplate.executeWithoutResult(status -> {
            List<Long> list = new ArrayList<>(configService.getLongList(configKey));
            argumentsParsed.forEach(list::remove);
            configService.putLongList(configKey, list);
        });
        sender.execute(new SendMessage(update.getMessage().getChatId().toString(), "OK"));
    }
}
