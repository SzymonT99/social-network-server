package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestSchoolDto {
    private String schoolType;
    private String name;
    private String startDate;
    private String graduationDate;
}
