package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserAccountDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    @JsonProperty(value = "isPublicProfile")
    private boolean isPublicProfile;
    private String phoneNumber;
    private Integer incorrectLoginCounter;
    private boolean activateAccount;
    @JsonProperty(value = "isBlocked")
    private boolean isBlocked;
    @JsonProperty(value = "isBanned")
    private boolean isBanned;
}
