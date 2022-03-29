package com.server.springboot.domain.dto.request;

import com.server.springboot.domain.dto.response.InterestDto;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestGroupDto {

    @NotEmpty
    @Size(max = 100)
    private String name;

    @NotEmpty
    private String description;

    @NotEmpty
    private String isPublic;

    private List<InterestDto> interests;
}
