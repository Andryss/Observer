package ru.andryss.observer.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.andryss.observer.repository.KeyStorageRepository;

/**
 * Service for working with key-value pairs
 */
@Service
@RequiredArgsConstructor
public class KeyStorageService {

    private final KeyStorageRepository keyStorageRepository;
    private final ObjectMapperWrapper objectMapper;

    /**
     * Put value by key. If key already had a value - replace it
     */
    public <T> void put(String key, T value) {
        String valueStr = objectMapper.writeValueAsString(value);
        keyStorageRepository.upsert(key, valueStr);
    }

    /**
     * Get value by key. If key doesn't have value - return default value
     */
    public <T> T get(String key, T defaultValue) {
        Optional<String> optional = keyStorageRepository.get(key);
        if (optional.isEmpty()) {
            return defaultValue;
        }
        return objectMapper.readValue(optional.get());
    }
}
