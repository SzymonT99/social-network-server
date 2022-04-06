package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.LikedPostDto;
import com.server.springboot.domain.dto.response.UserDto;
import com.server.springboot.domain.entity.LikedPost;
import com.server.springboot.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LikedPostDtoMapper implements Converter<LikedPostDto, LikedPost> {

    private final Converter<UserDto, User> userDtoMapper;

    @Autowired
    public LikedPostDtoMapper() {
        this.userDtoMapper = new UserDtoMapper();
    }

    @Override
    public LikedPostDto convert(LikedPost from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        return LikedPostDto.builder()
                .likedUser(userDtoMapper.convert(from.getLikedPostUser()))
                .date(from.getDate().format(formatter))
                .build();
    }
}
