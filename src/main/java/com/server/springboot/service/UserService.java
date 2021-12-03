package com.server.springboot.service;

import com.server.springboot.domain.dto.request.CreateUserDto;

public interface UserService {

    void addUser(CreateUserDto createUserDto);

    void activateAccount(String token);
}
