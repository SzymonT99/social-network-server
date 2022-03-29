package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.MessageType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChatMessageNotificationDto {
    private MessageType messageType;
    private String typingMessage;
    private Long chatId;
    private Long messageId;
    private UserDto author;
}
