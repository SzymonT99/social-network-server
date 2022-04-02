package com.server.springboot.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.server.springboot.domain.enumeration.ReportType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ReportDto {
    private Long reportId;
    private ReportType reportType;
    private String description;
    private String createdAt;
    @JsonProperty(value = "isConfirmed")
    private Boolean isConfirmed;
    private UserDto senderUser;
    private UserDto suspectUser;
}
