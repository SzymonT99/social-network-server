package com.server.springboot.domain.dto.request;

import com.server.springboot.domain.enumeration.MessageType;
import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestChatMessageDto {

    @NotNull
    private Long chatId;

    @NotNull
    private Long userId;

    private String message;

    private Long editedMessageId;

    @NotNull
    private MessageType messageType;
}
