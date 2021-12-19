package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestSchoolDto;
import com.server.springboot.domain.entity.School;
import com.server.springboot.domain.enumeration.SchoolType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class SchoolMapper implements Converter<School, RequestSchoolDto> {

    @Override
    public School convert(RequestSchoolDto from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return School.builder()
                .schoolType(SchoolType.valueOf(from.getSchoolType()))
                .name(from.getName())
                .startDate(LocalDate.parse(from.getStartDate(), formatter))
                .startDate(from.getGraduationDate() != null ? LocalDate.parse(from.getGraduationDate(), formatter) : null)
                .build();
    }
}