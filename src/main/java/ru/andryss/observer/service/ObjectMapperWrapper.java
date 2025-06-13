package ru.andryss.observer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * Class wrapping invocations of {@link ObjectMapper}. Hide exceptions and make typed deserialization easier
 */
@Service
@RequiredArgsConstructor
public class ObjectMapperWrapper {

    private final ObjectMapper mapper;

    /**
     * Serialize object to JSON string
     */
    public String writeValueAsString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    /**
     * Deserialize object from JSON string
     */
    @SneakyThrows
    public <T> T readValue(String data) {
        return mapper.readValue(data, new TypeReference<>() {
        });
    }
}
