package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.InterestDto;
import com.server.springboot.domain.entity.Interest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterestDtoListMapper implements Converter<List<InterestDto>, List<Interest>> {

    @Override
    public List<InterestDto> convert(List<Interest> from) {
        List<InterestDto> interestDtoList = new ArrayList<>();

        for (Interest interest : from) {
            InterestDto interestDto = InterestDto.builder()
                    .interestId(interest.getInterestId())
                    .name(interest.getName())
                    .build();

            interestDtoList.add(interestDto);
        }
        return interestDtoList;
    }
}