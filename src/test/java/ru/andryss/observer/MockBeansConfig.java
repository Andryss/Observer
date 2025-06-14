package ru.andryss.observer;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;

@Configuration
public class MockBeansConfig {

    @Bean
    public YandexGptApi yandexGptApi() {
        return Mockito.mock(YandexGptApi.class);
    }
}
