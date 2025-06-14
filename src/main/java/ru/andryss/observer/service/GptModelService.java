package ru.andryss.observer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.andryss.observer.facade.YandexGptFacade;
import ru.andryss.observer.model.MessageDto;

/**
 * Service for working with gpt models
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptModelService {

    private final YandexGptFacade yandexGptFacade;

    /**
     * Handle user chat message and return model answer
     */
    public MessageDto handleMessage(Long chatId, MessageDto userMessage) {
        log.info("Starting message handling from chat {}: {}", chatId, userMessage);

        // TODO: implement context saving
        String response = yandexGptFacade.generateAlternative(userMessage.text());

        MessageDto modelResponse = new MessageDto(response);
        log.info("Message from chat {} handled, model response: {}", chatId, modelResponse);
        return modelResponse;
    }
}
