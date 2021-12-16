package com.server.springboot.domain.dto.request;

import com.server.springboot.domain.enumeration.ReportType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestReportDto {
    private Long suspectId;
    private ReportType reportType;
    private String description;
}
