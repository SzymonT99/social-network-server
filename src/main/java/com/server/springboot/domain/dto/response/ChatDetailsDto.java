package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChatDetailsDto {
    private Long chatId;
    private String name;
    private String createdAt;
    private ImageDto image;
    private UserDto chatCreator;
    @JsonProperty(value = "isPrivate")
    private boolean isPrivate;
    private List<ChatMemberDto> chatMembers;
    private List<ChatMessageDto> messages;
}