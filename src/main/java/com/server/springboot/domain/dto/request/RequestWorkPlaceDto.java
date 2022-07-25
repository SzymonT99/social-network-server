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
public class RequestWorkPlaceDto {

    @NotEmpty
    private String company;

    @NotEmpty
    private String position;

    @NotEmpty
    private String startDate;

    private String endDate;
}
