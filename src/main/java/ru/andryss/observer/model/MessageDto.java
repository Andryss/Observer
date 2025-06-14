package ru.andryss.observer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private MessageRole role;
    private String text;

    public MessageDto(String text) {
        this.text = text;
    }
}
