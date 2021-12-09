package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
