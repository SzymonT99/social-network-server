package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.SchoolDto;
import com.server.springboot.domain.entity.School;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SchoolDtoListMapper implements Converter<List<SchoolDto>, List<School>>{

    @Override
    public List<SchoolDto> convert(List<School> from) {
        List<SchoolDto> schoolDtoList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (School school : from) {
            SchoolDto schoolDto = SchoolDto.builder()
                    .schoolId(school.getSchoolId())
                    .schoolType(school.getSchoolType())
                    .name(school.getName())
                    .startDate(school.getStartDate().format(formatter))
                    .graduationDate(school.getGraduationDate() != null ? school.getGraduationDate().format(formatter) : null)
                    .build();
            schoolDtoList.add(schoolDto);
        }
        return schoolDtoList;
    }
}
