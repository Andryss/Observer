package ru.andryss.observer.service;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.andryss.observer.model.ConfigKey;

import static ru.andryss.observer.model.ConfigKey.isBooleanType;
import static ru.andryss.observer.model.ConfigKey.isLongListType;
import static ru.andryss.observer.model.ConfigKey.isStringType;

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
        if (!isStringType(key)) {
            throw new IllegalArgumentException(key.name() + " is not string");
        }
        return getInternal(key);
    }

    /**
     * Retrieves boolean config value by key
     */
    public boolean getBoolean(ConfigKey key) {
        ensureBooleanType(key);
        return getInternal(key);
    }

    /**
     * Sets boolean config value
     */
    public void putBoolean(ConfigKey key, boolean value) {
        ensureBooleanType(key);
        putInternal(key, value);
    }

    /**
     * Retrieves long list config value by key
     */
    public List<Long> getLongList(ConfigKey key) {
        ensureLongListType(key);
        return getInternal(key);
    }

    /**
     * Sets long list config value
     */
    public void putLongList(ConfigKey key, List<Long> value) {
        ensureLongListType(key);
        putInternal(key, value);
    }

    /**
     * Retrieves raw config value by key
     */
    public String getRawString(ConfigKey key) {
        return keyStorageService.getString(key.getKey(), key.getDefaultValue());
    }

    private <T> T getInternal(ConfigKey key) {
        Object defaultValue = key.getDefaultValue();
        //noinspection unchecked
        return keyStorageService.get(key.getKey(), (T) defaultValue, ((TypeReference<T>) key.getType()));
    }

    private <T> void putInternal(ConfigKey key, T value) {
        keyStorageService.put(key.getKey(), value);
    }

    private static void ensureBooleanType(ConfigKey key) {
        if (!isBooleanType(key)) {
            throw new IllegalArgumentException(key.name() + " is not boolean");
        }
    }

    private static void ensureLongListType(ConfigKey key) {
        if (!isLongListType(key)) {
            throw new IllegalArgumentException(key.name() + " is not long list");
        }
    }

}
