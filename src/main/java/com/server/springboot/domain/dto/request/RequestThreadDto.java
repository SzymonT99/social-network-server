package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestThreadDto {
    private Long groupMemberId;
    private String title;
    private String content;
}
