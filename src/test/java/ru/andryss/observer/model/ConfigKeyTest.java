package ru.andryss.observer.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.andryss.observer.BaseDbTest;
import ru.andryss.observer.service.ObjectMapperWrapper;

class ConfigKeyTest extends BaseDbTest {

    @Autowired
    ObjectMapperWrapper mapper;

    @Test
    void testDefaultValueTypes() {
        for (ConfigKey config : ConfigKey.values()) {
            Type type = config.getType().getType();
            Object defaultValue = config.getDefaultValue();

            if (type instanceof Class<?>) {
                Assertions.assertThat(((Class<?>) type).isInstance(defaultValue)).isTrue();
                continue;
            }

            if (type instanceof ParameterizedType) {
                Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
                Assertions.assertThat(rawType.isInstance(defaultValue)).isTrue();
                continue;
            }

            throw new IllegalStateException("Unknown ConfigKey type " + type);
        }
    }
}