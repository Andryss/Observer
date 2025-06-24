package ru.andryss.observer.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.andryss.observer.model.ConfigKey;

/**
 * Service for working with app configurations
 */
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final KeyStorageService keyStorageService;

    /**
     * Retrieves string config value by key
     */
    public String getString(ConfigKey key) {
        return getInternal(key);
    }

    /**
     * Retrieves boolean config value by key
     */
    public boolean getBoolean(ConfigKey key) {
        return getInternal(key);
    }

    /**
     * Retrieves long list config value by key
     */
    public List<Long> getLongList(ConfigKey key) {
        return getInternal(key);
    }

    private <T> T getInternal(ConfigKey key) {
        Object defaultValue = key.getDefaultValue();
        //noinspection unchecked
        return keyStorageService.get(key.getKey(), (T) defaultValue, ((TypeReference<T>) key.getType()));
    }

}
