package com.server.springboot.service;

import com.server.springboot.domain.dto.response.BoardActivityItemDto;
import com.server.springboot.domain.dto.response.NotificationDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserActivityService {

    List<BoardActivityItemDto> findUserActivityBoard();

    @Transactional
    List<NotificationDto> findUserNotifications();
}
