package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.springboot.domain.enumeration.MessageType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChatMessageDto {
    private Long messageId;
    private String text;
    private MessageType messageType;
    private ImageDto image;
    private String createdAt;
    private String editedAt;
    @JsonProperty(value = "isEdited")
    private boolean isEdited;
    @JsonProperty(value = "isDeleted")
    private boolean isDeleted;
    private UserDto author;
}
