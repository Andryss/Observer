package ru.andryss.observer;

import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andryss.observer.config.BotProperties;
import ru.andryss.observer.executor.UpdateExecutor;

@Slf4j
@Component
public class UpdateDispatcher extends TelegramLongPollingBot {

    private final BotProperties properties;
    private final List<UpdateExecutor> executors;

    public UpdateDispatcher(
            BotProperties botProperties,
            List<UpdateExecutor> executors
    ) {
        super(botProperties.getToken());
        this.properties = botProperties;
        this.executors = executors;
    }

    @Override
    public String getBotUsername() {
        return properties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Update received {}", update);

        Integer updateId = update.getUpdateId();
        if (needToSkip(update)) {
            log.info("Update {} skipped", updateId);
            return;
        }

        for (UpdateExecutor executor : executors) {
            if (!executor.isActive()) {
                continue;
            }
            if (!executor.canProcess(update)) {
                continue;
            }
            try {
                log.info("Submitting update {} for execution on {}", updateId, executor);
                executor.process(update, this);
            } catch (Exception e) {
                log.error("Executor {} failed with error on update {}: {}", executor, updateId, e.getMessage(), e);
            }
        }
        log.info("Update {} handled", updateId);
    }

    private boolean needToSkip(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Instant messageTimestamp = Instant.ofEpochSecond(message.getDate());
            if (messageTimestamp.isBefore(Instant.now().minusSeconds(properties.getExpirationSeconds()))) {
                log.info("Update {} message is too old {}, skipping", update.getUpdateId(), messageTimestamp);
                return true;
            }
        }

        return false;
    }
}
