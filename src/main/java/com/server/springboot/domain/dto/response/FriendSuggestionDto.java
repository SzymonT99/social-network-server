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
public class FriendSuggestionDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private ProfilePhotoDto profilePhoto;
    private AddressDto address;
    private List<UserDto> userFriends;
    private List<UserDto> mutualFriends;
}

