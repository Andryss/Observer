package ru.andryss.observer;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andryss.observer.config.BotProperties;

@Slf4j
@Component
public class UpdateDispatcher extends TelegramLongPollingBot implements DisposableBean {

    @Getter
    private final String botUsername;
    private final int messageExpirationSeconds;
    private final ExecutorService updateExecutors;

    public UpdateDispatcher(
            BotProperties botProperties,
            ExecutorService updateExecutors
    ) {
        super(botProperties.getToken());
        this.botUsername = botProperties.getUsername();
        this.messageExpirationSeconds = botProperties.getExpirationSeconds();
        this.updateExecutors = updateExecutors;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Integer updateId = update.getUpdateId();
        log.debug("Update {} received", updateId);

        if (needToSkip(update)) {
            log.debug("Update {} skipped", updateId);
            return;
        }

        updateExecutors.submit(() -> handleUpdate(update));
        log.debug("Update {} submitted for execution", updateId);
    }

    private boolean needToSkip(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Instant messageTimestamp = Instant.ofEpochSecond(message.getDate());
            if (messageTimestamp.isBefore(Instant.now().minusSeconds(messageExpirationSeconds))) {
                log.debug("Update {} message is too old {}, skipping", update.getUpdateId(), messageTimestamp);
                return true;
            }
        }

        return false;
    }

    private void handleUpdate(Update update) {
        log.info("Received update {}: {}", update.getUpdateId(), update);
    }

    @Override
    public void destroy() {
        updateExecutors.shutdownNow();
    }
}
