package com.server.springboot.domain.dto.request;

import com.server.springboot.domain.enumeration.MessageType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChatMessage {
    private String message;
    private MessageType messageType;
    private Long chatId;
    private Long userId;
}
