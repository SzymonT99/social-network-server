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
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
}
