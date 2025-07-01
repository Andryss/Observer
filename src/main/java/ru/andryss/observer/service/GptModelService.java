package ru.andryss.observer.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.andryss.observer.config.YandexGptProperties;
import ru.andryss.observer.facade.YandexGptFacade;
import ru.andryss.observer.model.ChatContextEntity;
import ru.andryss.observer.model.MessageDto;
import ru.andryss.observer.model.MessageRole;
import ru.andryss.observer.repository.ChatContextRepository;

import static ru.andryss.observer.model.ConfigKey.MODEL_INSTRUCTION;

/**
 * Service for working with gpt models
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptModelService {

    private final ChatContextRepository chatContextRepository;
    private final YandexGptFacade yandexGptFacade;
    private final Clock clock;
    private final YandexGptProperties properties;
    private final ConfigService configService;

    /**
     * Handle user chat message and return model answer
     */
    public MessageDto handleMessage(String chatId, MessageDto userMessage) {
        log.info("Starting message handling from chat {}: {}", chatId, userMessage);

        ChatContextEntity context = chatContextRepository.findByChatId(chatId)
                .orElseGet(() -> createContext(chatId));

        userMessage.setRole(MessageRole.USER); // explicit set role

        String response = yandexGptFacade.generateAlternative(context.getMessages(), userMessage);

        ArrayList<MessageDto> newMessagesContext = new ArrayList<>(context.getMessages());
        newMessagesContext.add(userMessage);
        newMessagesContext.add(new MessageDto(MessageRole.ASSISTANT, response));

        while (newMessagesContext.size() > properties.getContextMessagesCount()) {
            // remove message from index 1 to save first instruction message
            newMessagesContext.remove(1);
        }

        context.setMessages(newMessagesContext);

        chatContextRepository.upsert(context);

        MessageDto modelResponse = new MessageDto(response);
        log.info("Message from chat {} handled, model response: {}", chatId, modelResponse);
        return modelResponse;
    }

    /**
     * Clear chat saved context if exists
     */
    public void clearContext(String chatId) {
        log.info("Clearing context for chat {}", chatId);
        chatContextRepository.deleteByChatId(chatId);
    }

    private ChatContextEntity createContext(String chatId) {
        ChatContextEntity context = new ChatContextEntity();
        context.setChatId(chatId);
        context.setMessages(List.of(
                new MessageDto(MessageRole.SYSTEM, configService.getString(MODEL_INSTRUCTION))
        ));
        Instant now = Instant.now(clock);
        context.setUpdatedAt(now);
        context.setCreatedAt(now);
        return context;
    }
}
