package com.server.springboot.service;

import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.ActionType;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationService {
    void sendNotificationToUser(User sender, Long recipientId, ActionType actionType);
}
