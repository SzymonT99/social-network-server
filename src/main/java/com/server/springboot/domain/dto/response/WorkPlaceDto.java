package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class WorkPlaceDto {
    private Long workPlaceId;
    private String company;
    private String position;
    private String startDate;
    private String endDate;
}
