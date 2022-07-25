package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.SchoolType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class SchoolDto {
    private Long schoolId;
    private SchoolType schoolType;
    private String name;
    private String startDate;
    private String graduationDate;
}
