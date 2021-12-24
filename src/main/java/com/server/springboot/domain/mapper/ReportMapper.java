package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestReportDto;
import com.server.springboot.domain.entity.Report;
import com.server.springboot.domain.enumeration.ReportType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReportMapper implements Converter<Report, RequestReportDto>{

    @Override
    public Report convert(RequestReportDto from) {
        return Report.builder()
                .reportType(ReportType.valueOf(from.getReportType()))
                .description(from.getDescription())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
