package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestAddressDto {

    @NotEmpty
    @Size(max = 50)
    private String country;

    @NotEmpty
    @Size(max = 30)
    private String city;

    @Size(max = 30)
    private String street;

    @NotEmpty
    @Size(max = 10)
    private String zipCode;
}
