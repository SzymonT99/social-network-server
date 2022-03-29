package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class GroupMemberForumStatsDto {
    private Long groupMemberId;
    private UserDto user;
    private Integer threadsNumber;
    private Integer answersNumber;
    private Float answersAverageRating;
}
