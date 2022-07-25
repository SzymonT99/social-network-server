package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestReportDto {
    @NotNull
    private Long suspectId;

    @NotNull
    private String reportType;
    private String description;
}
