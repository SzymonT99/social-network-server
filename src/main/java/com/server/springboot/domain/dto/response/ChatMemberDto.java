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
    private UserDto user;
    private String addedIn;
    private boolean hasMutedChat;
    private boolean canAddOthers;
}
