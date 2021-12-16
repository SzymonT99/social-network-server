package com.server.springboot.domain.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UpdateAddressDto {
    private String country;
    private String city;
    private String street;
    private String zipCode;
}
