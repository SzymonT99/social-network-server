package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestEventDto {

    @NotNull
    private Long userId;

    @NotNull
    private AddressDto eventAddress;

    @NotEmpty
    private String title;

    @NotEmpty
    private String description;

    @NotEmpty
    private String eventDate;
}
