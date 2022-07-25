package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestUserFavouriteDto;
import com.server.springboot.domain.entity.UserFavourite;
import com.server.springboot.domain.enumeration.FavouriteType;
import org.springframework.stereotype.Component;

@Component
public class UserFavouriteMapper implements Converter<UserFavourite, RequestUserFavouriteDto> {

    @Override
    public UserFavourite convert(RequestUserFavouriteDto from) {
        return UserFavourite.builder()
                .name(from.getName())
                .favouriteType(FavouriteType.valueOf(from.getFavouriteType()))
                .build();
    }
}
