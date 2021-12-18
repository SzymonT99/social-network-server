package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestWorkPlaceDto;
import com.server.springboot.domain.entity.WorkPlace;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class WorkPlaceMapper implements Converter<WorkPlace, RequestWorkPlaceDto>{

    @Override
    public WorkPlace convert(RequestWorkPlaceDto from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return WorkPlace.builder()
                .company(from.getCompany())
                .position(from.getPosition())
                .startDate(LocalDate.parse(from.getStartDate(), formatter))
                .endDate(from.getEndDate() != null ? LocalDate.parse(from.getEndDate(), formatter) : null)
                .build();
    }
}
