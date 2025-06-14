package ru.andryss.observer.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.andryss.observer.model.ChatContextEntity;
import ru.andryss.observer.service.ObjectMapperWrapper;

@Repository
@RequiredArgsConstructor
public class ChatContextRepository implements InitializingBean {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapperWrapper objectMapper;

    private RowMapper<ChatContextEntity> rowMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        rowMapper = (rs, rowNum) -> {
            ChatContextEntity context = new ChatContextEntity();
            context.setChatId(rs.getString("chat_id"));
            context.setMessages(objectMapper.readValue(rs.getString("messages"), new TypeReference<>() {
            }));
            context.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
            context.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            return context;
        };
    }

    /**
     * Insert or update chat context by chat id
     */
    public void upsert(ChatContextEntity entity) {
        jdbcTemplate.update("""
                insert into chat_contexts(chat_id, messages, updated_at, created_at)
                    values (:chatId, :messages::jsonb, :updatedAt, :createdAt)
                on conflict (chat_id) do update set messages = excluded.messages, updated_at = excluded.updated_at
                """, new MapSqlParameterSource()
                .addValue("chatId", entity.getChatId())
                .addValue("messages", objectMapper.writeValueAsString(entity.getMessages()))
                .addValue("updatedAt", Timestamp.from(entity.getUpdatedAt()))
                .addValue("createdAt", Timestamp.from(entity.getCreatedAt())));
    }

    /**
     * Select chat context by chat id
     */
    public Optional<ChatContextEntity> findByChatId(String chatId) {
        List<ChatContextEntity> contexts = jdbcTemplate.query("""
                select * from chat_contexts
                where chat_id = :chatId
                """, new MapSqlParameterSource()
                .addValue("chatId", chatId), rowMapper);

        if (contexts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(contexts.get(0));
    }
}
