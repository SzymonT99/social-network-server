package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.AddressDto;
import com.server.springboot.domain.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressDtoMapper implements Converter<AddressDto, Address> {

    @Override
    public AddressDto convert(Address from) {
        return AddressDto.builder()
                .addressId(from.getAddressId())
                .country(from.getCountry())
                .city(from.getCity())
                .street(from.getStreet())
                .zipCode(from.getZipCode())
                .build();
    }
}
