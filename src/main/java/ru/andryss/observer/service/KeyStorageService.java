package ru.andryss.observer.service;

import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
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
     * Get value by key with special type. If key doesn't have value - return default value
     */
    public <T> T get(String key, T defaultValue, TypeReference<T> type) {
        return getRawValue(key)
                .map(value -> objectMapper.readValue(value, type))
                .orElse(defaultValue);
    }

    /**
     * Get value by key with special type. If key doesn't have value - return default value
     */
    public String getString(String key, Object defaultValue) {
        return getRawValue(key)
                .orElseGet(() -> objectMapper.writeValueAsString(defaultValue));
    }

    private Optional<String> getRawValue(String key) {
        return keyStorageRepository.get(key);
    }
}
