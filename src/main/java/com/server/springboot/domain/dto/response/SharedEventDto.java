package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class SharedEventDto {
    private UserDto authorOfSharing;
    private String sharingDate;
    private EventDto event;
}
