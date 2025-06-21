package ru.andryss.observer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
@RequiredArgsConstructor
public class BotUserInfoProviderService {

    private final ObjectProvider<User> botUserInfoProvider;

    public User getBotUser() {
        return botUserInfoProvider.getObject();
    }
}
