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
public class RequestChatMessageDto {
    private Long chatId;
    private Long userId;
    private String message;
    private MessageType messageType;
}
