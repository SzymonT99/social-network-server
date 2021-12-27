package com.server.springboot.service;

import com.server.springboot.domain.dto.response.BoardActivityItemDto;

import java.util.List;

public interface UserActivityService {

    List<BoardActivityItemDto> findUserActivityBoard();
}
