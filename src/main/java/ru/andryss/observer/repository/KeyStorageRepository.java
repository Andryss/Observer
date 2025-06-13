package ru.andryss.observer.repository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository for working with key_storage table
 */
@Repository
@RequiredArgsConstructor
public class KeyStorageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    /**
     * Insert or update value by the key
     */
    public void upsert(String key, String value) {
        jdbcTemplate.update("""
                insert into key_storage(key, value)
                    values (:key, :value)
                on conflict (key) do update set value = excluded.value
                """, new MapSqlParameterSource()
                        .addValue("key", key)
                        .addValue("value", value));
    }

    /**
     * Select value by the key
     */
    public Optional<String> get(String key) {
        List<String> result = jdbcTemplate.queryForList("""
                select value from key_storage
                where key = :key
                """, new MapSqlParameterSource()
                        .addValue("key", key), String.class);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }
}
