package ru.andryss.observer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.andryss.observer.facade.YandexGptFacade;
import ru.andryss.observer.model.MessageDto;

/**
 * Service for working with gpt models
 */
@Service
@RequiredArgsConstructor
public class GptModelService {

    private final YandexGptFacade yandexGptFacade;

    /**
     * Handle user chat message and return model answer
     */
    public MessageDto handleMessage(Long chatId, MessageDto userMessage) {
        // TODO: implement context saving
        String response = yandexGptFacade.generateAlternative(userMessage.text());

        return new MessageDto(response);
    }
}
