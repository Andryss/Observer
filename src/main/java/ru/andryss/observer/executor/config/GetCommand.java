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
public class GetCommand implements ConfigCommand {

    private static final String MESSAGE_FORMAT = """
            ```json
            %s
            ```
            """;

    private final ConfigService configService;

    @Getter
    private final ConfigCommandInfo configCommandInfo = new ConfigCommandInfo("get");

    @Override
    public void execute(List<String> arguments, Update update, AbsSender sender) throws Exception {
        String chatId = update.getMessage().getChatId().toString();

        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("No key argument");
        }

        ConfigKey configKey = ConfigKey.valueOf(arguments.get(0));

        String value = configService.getRawString(configKey);
        SendMessage sendMessage = new SendMessage(chatId, MESSAGE_FORMAT.formatted(value));
        sendMessage.enableMarkdown(true);
        sender.execute(sendMessage);
    }
}
