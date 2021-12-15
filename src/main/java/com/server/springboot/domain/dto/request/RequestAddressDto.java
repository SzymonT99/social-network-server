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
public class RequestAddressDto {

    @NotEmpty
    private String country;

    @NotEmpty
    private String city;

    private String street;

    @NotEmpty
    private String zipCode;
}
