package ru.andryss.observer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.andryss.observer.generated.yandexgpt.api.YandexGptApi;
import ru.andryss.observer.generated.yandexgpt.invoker.ApiClient;

@Configuration
@Profile("!functionalTest")
public class YandexGptConfiguration {

    @Bean
    public YandexGptApi yandexGptApi(YandexGptProperties properties) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBearerToken(properties.getApiKey());
        return new YandexGptApi(apiClient);
    }
}
