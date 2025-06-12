package ru.andryss.observer;

import java.util.concurrent.ExecutorService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andryss.observer.config.BotProperties;

@Slf4j
@Component
public class UpdateDispatcher extends TelegramLongPollingBot implements DisposableBean {

    @Getter
    private final String botUsername;
    private final ExecutorService updateExecutors;

    public UpdateDispatcher(
            BotProperties botProperties,
            ExecutorService updateExecutors
    ) {
        super(botProperties.getToken());
        this.botUsername = botProperties.getUsername();
        this.updateExecutors = updateExecutors;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Integer updateId = update.getUpdateId();
        log.debug("Update {} received", updateId);
        updateExecutors.submit(() -> handleUpdate(update));
        log.debug("Update {} submitted for execution", updateId);
    }

    private void handleUpdate(Update update) {
        log.info("Received update {}: {}", update.getUpdateId(), update);
    }

    @Override
    public void destroy() {
        updateExecutors.shutdownNow();
    }
}
