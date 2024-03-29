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
public class SharedPostInfoDto {
    private Long shardPostId;
    private String sharingText;
    private UserDto authorOfSharing;
    private String date;
    @JsonProperty(value = "isPublic")
    private boolean isPublic;
}
