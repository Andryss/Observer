package ru.andryss.observer.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.andryss.observer.service.ConfigService;
import ru.andryss.observer.service.GptModelService;

import static ru.andryss.observer.model.ConfigKey.ADMIN_USER_IDS;
import static ru.andryss.observer.model.ConfigKey.CLEAR_CONTEXT_COMMAND_EXECUTOR_ACTIVE;

@Component
@RequiredArgsConstructor
public class ClearContextCommandExecutor extends AbstractCommandExecutor {

    private final ConfigService configService;
    private final GptModelService gptModelService;

    @Getter
    private final CommandInfo commandInfo = new CommandInfo("/clearcontext");

    @Override
    public boolean isActive() {
        return configService.getBoolean(CLEAR_CONTEXT_COMMAND_EXECUTOR_ACTIVE);
    }

    @Override
    public boolean canProcess(Update update) {
        return super.canProcess(update) &&
                configService.getLongList(ADMIN_USER_IDS).contains(update.getMessage().getFrom().getId());
    }

    @Override
    public void process(Update update, AbsSender sender) throws Exception {
        String chatId = update.getMessage().getChatId().toString();
        gptModelService.clearContext(chatId);
        sender.execute(new SendMessage(chatId, "Чистый, как совесть младенца"));
    }
}
