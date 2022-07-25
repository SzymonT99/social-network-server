package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class AddressDto {
    private Long addressId;
    private String country;
    private String city;
    private String street;
    private String zipCode;
}
