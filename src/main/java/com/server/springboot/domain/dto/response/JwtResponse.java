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
public class JwtResponse {
    private Long userId;
    private List<String> roles;
    private String accessToken;
    private String tokenType;
    private Long accessTokenExpiresIn;
    private String refreshToken;
}
