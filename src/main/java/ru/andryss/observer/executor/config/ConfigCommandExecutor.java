package ru.andryss.observer.executor.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.executor.AbstractCommandExecutor;
import ru.andryss.observer.service.ConfigService;

import static ru.andryss.observer.model.ConfigKey.ADMIN_USER_IDS;
import static ru.andryss.observer.model.ConfigKey.CONFIG_COMMAND_EXECUTOR_ACTIVE;

@Component
@RequiredArgsConstructor
public class ConfigCommandExecutor extends AbstractCommandExecutor implements InitializingBean {

    private final ConfigService configService;
    private final List<ConfigCommand> commandList;
    private final Map<String, ConfigCommand> commandMap = new HashMap<>();

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/config");

    @Override
    public void afterPropertiesSet() {
        for (ConfigCommand command : commandList) {
            commandMap.put(command.getConfigCommandInfo().command(), command);
        }
    }

    @Override
    public boolean isActive() {
        return configService.getBoolean(CONFIG_COMMAND_EXECUTOR_ACTIVE);
    }

    @Override
    public boolean canProcess(Update update) {
        return super.canProcess(update) &&
                configService.getLongList(ADMIN_USER_IDS).contains(update.getMessage().getFrom().getId());
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        Message message = update.getMessage();

        String[] parts = message.getText().split("\\s");
        if (parts.length < 2) {
            sender.execute(new SendMessage(message.getChatId().toString(), "Must be at least one arguments"));
            return;
        }

        String commandName = parts[1];
        ConfigCommand command = commandMap.get(commandName);
        if (command == null) {
            sender.execute(new SendMessage(message.getChatId().toString(), "Unknown command " + commandName));
            return;
        }

        List<String> arguments = new ArrayList<>();
        for (int i = 2; i < parts.length; i++) {
            String part = parts[i];
            if (!StringUtils.isBlank(part)) {
                arguments.add(part);
            }
        }

        try {
            command.execute(arguments, update, sender);
        } catch (Exception e) {
            sender.execute(new SendMessage(message.getChatId().toString(), "ERROR: " + e.getMessage()));
        }
    }
}
