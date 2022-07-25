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
public class ChatDto {
    private Long chatId;
    private String name;
    private String createdAt;
    private String activityDate;
    private String lastMessage;
    private UserDto lastMessageAuthor;
    private Integer newMessages;
    private UserDto chatCreator;
    private ImageDto image;
    @JsonProperty(value = "isPrivate")
    private boolean isPrivate;
    private List<ChatMemberDto> members;
}
