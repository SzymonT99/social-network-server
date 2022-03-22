package com.server.springboot.domain.dto.response;

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
    private Integer newMessages;
    private UserDto chatCreator;
    private List<ChatMemberDto> chatMembers;
    private ImageDto image;
}
