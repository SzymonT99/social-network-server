package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class LikedPostDto {
    private Long userId;
    private String username;
    private String date;
}
