package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.ReportDto;
import com.server.springboot.domain.dto.response.UserDetailsDto;
import com.server.springboot.domain.entity.Report;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ReportType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReportDtoListMapper implements Converter<List<ReportDto>, List<Report>> {

    private final Converter<UserDetailsDto, User> userDetailsDtoMapper;

    @Autowired
    public ReportDtoListMapper(Converter<UserDetailsDto, User> userDetailsDtoMapper) {
        this.userDetailsDtoMapper = userDetailsDtoMapper;
    }

    @Override
    public List<ReportDto> convert(List<Report> from) {
        List<ReportDto> reportDtoList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        for (Report report : from) {
            ReportDto reportDto = ReportDto.builder()
                    .reportId(report.getReportId())
                    .reportType(report.getReportType())
                    .description(report.getDescription())
                    .createdAt(report.getCreatedAt().format(formatter))
                    .confirmed(report.isConfirmation())
                    .suspectDetails(userDetailsDtoMapper.convert(report.getSuspect()))
                    .build();

            reportDtoList.add(reportDto);
        }
        return reportDtoList;
    }
}
