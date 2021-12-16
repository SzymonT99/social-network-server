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
    private String title;
    private String createdAt;
    private String chatCreatorName;
    private Long chatCreatorId;
    private List<ChatMessageDto> messages;
    private List<ChatMemberDto> chatMembers;
}
