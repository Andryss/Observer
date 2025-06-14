package ru.andryss.observer.model;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatContextEntity {
    private String chatId;
    private List<MessageDto> messages;
    private Instant updatedAt;
    private Instant createdAt;
}
