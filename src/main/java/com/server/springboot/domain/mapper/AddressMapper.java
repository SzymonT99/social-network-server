package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestAddressDto;
import com.server.springboot.domain.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper implements Converter<Address, RequestAddressDto> {

    @Override
    public Address convert(RequestAddressDto from) {
        return Address.builder()
                .country(from.getCountry())
                .city(from.getCity())
                .street(from.getStreet())
                .zipCode(from.getZipCode())
                .build();
    }
}
