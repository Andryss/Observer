package ru.andryss.observer.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.andryss.observer.facade.YandexGptFacade;
import ru.andryss.observer.model.ChatContextEntity;
import ru.andryss.observer.model.MessageDto;
import ru.andryss.observer.model.MessageRole;
import ru.andryss.observer.repository.ChatContextRepository;

/**
 * Service for working with gpt models
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptModelService {

    private static final String DEFAULT_MODEL_INSTRUCTIONS = """
            Будь как хороший друг — общайся просто, по-человечески, без официоза.
            Отвечай кратко и по сути, не больше пары предложений, не занудствуй.
            Поддерживай разговор, интересуйся собеседником, шутки и мемы — по настроению.
            Не строй из себя всезнайку.
            """;
    private static final int DEFAULT_MESSAGE_COUNT_CONTEXT_WINDOW = 11;

    private final ChatContextRepository chatContextRepository;
    private final YandexGptFacade yandexGptFacade;

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

        while (newMessagesContext.size() > DEFAULT_MESSAGE_COUNT_CONTEXT_WINDOW) {
            // remove message from index 1 to save first instruction message
            newMessagesContext.remove(1);
        }

        context.setMessages(newMessagesContext);

        chatContextRepository.upsert(context);

        MessageDto modelResponse = new MessageDto(response);
        log.info("Message from chat {} handled, model response: {}", chatId, modelResponse);
        return modelResponse;
    }

    private ChatContextEntity createContext(String chatId) {
        ChatContextEntity context = new ChatContextEntity();
        context.setChatId(chatId);
        context.setMessages(List.of(
                new MessageDto(MessageRole.SYSTEM, DEFAULT_MODEL_INSTRUCTIONS)
        ));
        context.setUpdatedAt(Instant.now()); // TODO: migrate to clock
        context.setCreatedAt(Instant.now()); // TODO: migrate to clock
        return context;
    }
}
