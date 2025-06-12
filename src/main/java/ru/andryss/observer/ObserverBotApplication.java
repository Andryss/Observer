package ru.andryss.observer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.andryss.observer.jacoco.ExcludeFromJacocoGeneratedReport;

@SpringBootApplication
public class ObserverBotApplication {

    /**
     * Application entry point. Runs spring boot application context.
     * Then registers bot in telegram ({@link LongPollingBot} instance).
     * @throws TelegramApiException if some error occurred during bot registration
     */
    @ExcludeFromJacocoGeneratedReport
    public static void main(String[] args) throws TelegramApiException {
        ConfigurableApplicationContext context = SpringApplication.run(ObserverBotApplication.class, args);
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean(LongPollingBot.class));
    }

}
