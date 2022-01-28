package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.WorkPlaceDto;
import com.server.springboot.domain.entity.WorkPlace;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkPlaceDtoListMapper implements Converter<List<WorkPlaceDto>, List<WorkPlace>>{

    @Override
    public List<WorkPlaceDto> convert(List<WorkPlace> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<WorkPlaceDto> workPlaceDtoList = new ArrayList<>();

        for (WorkPlace workPlace : from) {
            WorkPlaceDto workPlaceDto = WorkPlaceDto.builder()
                    .workPlaceId(workPlace.getWorkPlaceId())
                    .company(workPlace.getCompany())
                    .position(workPlace.getPosition())
                    .startDate(workPlace.getStartDate().format(formatter))
                    .endDate(workPlace.getEndDate() != null ? workPlace.getEndDate().format(formatter) : null)
                    .build();
            workPlaceDtoList.add(workPlaceDto);
        }
        return workPlaceDtoList;
    }
}
