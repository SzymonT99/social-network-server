package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ChatMemberDto {
    private Long chatMemberId;
    private String chatMemberName;
    private String createdAt;
    private boolean hasMutedChat;
    private boolean hasUnreadMessage;
    private boolean canAddOthers;
}
