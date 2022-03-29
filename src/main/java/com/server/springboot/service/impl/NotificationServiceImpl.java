package com.server.springboot.service.impl;

import com.server.springboot.domain.dto.response.ActionNotificationDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActionType;
import com.server.springboot.domain.mapper.Converter;
import com.server.springboot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Converter<UserDto, User> userDtoMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public NotificationServiceImpl(Converter<UserDto, User> userDtoMapper, SimpMessagingTemplate simpMessagingTemplate) {
        this.userDtoMapper = userDtoMapper;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void sendNotificationToUser(User sender, Long recipientId, ActionType actionType) {
        simpMessagingTemplate.convertAndSendToUser(
                recipientId.toString(), "/queue/messages",
                new ActionNotificationDto(
                        userDtoMapper.convert(sender),
                        actionType,
                        LocalDateTime.now().toString()
                ));
    }
}
