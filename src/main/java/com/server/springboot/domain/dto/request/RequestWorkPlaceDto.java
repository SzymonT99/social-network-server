package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestWorkPlaceDto {
    private String company;
    private String position;
    private String startDate;
    private String endDate;
}
