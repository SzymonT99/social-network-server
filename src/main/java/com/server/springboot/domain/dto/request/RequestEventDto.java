package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestEventDto {

    @NotNull
    private RequestAddressDto eventAddress;

    @NotEmpty
    @Size(max = 30)
    private String title;

    @NotEmpty
    private String description;

    @NotEmpty
    private String eventDate;
}
