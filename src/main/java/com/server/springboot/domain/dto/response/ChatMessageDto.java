package com.server.springboot.domain.dto.response;

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
    private Long messageAuthorId;
    private String messageAuthorName;
    private String text;
    private ImageDto image;
    private String createdAt;
    private String editedAt;
    private boolean isEdited;
}
