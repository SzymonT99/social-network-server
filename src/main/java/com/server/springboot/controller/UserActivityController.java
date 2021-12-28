package com.server.springboot.controller;

import com.server.springboot.domain.dto.response.BoardActivityItemDto;
import com.server.springboot.domain.dto.response.NotificationDto;
import com.server.springboot.service.UserActivityService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApiController.class);
    private final UserActivityService userActivityService;

    @Autowired
    public UserActivityController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @ApiOperation(value = "Get logged user activity board")
    @GetMapping(value = "/activity")
    public ResponseEntity<List<BoardActivityItemDto>> getUserActivityBoard() {
        LOGGER.info("---- User get own activity board");
        return new ResponseEntity<>(userActivityService.findUserActivityBoard(), HttpStatus.OK);
    }

    @ApiOperation(value = "Get logged user notifications")
    @GetMapping(value = "/activity/notifications")
    public ResponseEntity<List<NotificationDto>> getUserNotifications() {
        LOGGER.info("---- User get own activity board");
        return new ResponseEntity<>(userActivityService.findUserNotifications(), HttpStatus.OK);
    }
}
