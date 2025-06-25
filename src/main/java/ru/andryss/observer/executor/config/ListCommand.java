package ru.andryss.observer.executor.config;

import java.util.List;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.model.ConfigKey;

@Component
public class ListCommand implements ConfigCommand {

    @Getter
    private final ConfigCommandInfo configCommandInfo = new ConfigCommandInfo("list");

    @Override
    public void execute(List<String> arguments, Update update, AbsSender sender) throws Exception {
        StringBuilder builder = new StringBuilder();

        for (ConfigKey configKey : ConfigKey.values()) {
            builder.append("`").append(configKey.name()).append("` -- (")
                    .append(configKey.getType().getType().getTypeName()).append(")\n");
        }

        SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), builder.toString());
        sendMessage.enableMarkdown(true);
        sender.execute(sendMessage);
    }
}
