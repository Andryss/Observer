package ru.andryss.observer.executor.config;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Interface describing "/config" command
 */
public interface ConfigCommand {

    /**
     * Returns object describing command. See {@link ConfigCommandInfo}
     */
    ConfigCommandInfo getConfigCommandInfo();

    /**
     * Method containing command logic. Method invokes when command name is next to /config command
     */
    void execute(List<String> arguments, Update update, AbsSender sender) throws Exception;

    /**
     * Record object describing config command
     * @param command name of the command
     */
    record ConfigCommandInfo(String command) {
    }
}
