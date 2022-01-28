package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestSchoolDto {

    @NotEmpty
    private String schoolType;

    @NotEmpty
    private String name;

    @NotEmpty
    private String startDate;

    private String graduationDate;
}
