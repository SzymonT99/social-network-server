package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.UserFavouriteDto;
import com.server.springboot.domain.entity.UserFavourite;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserFavouriteDtoListMapper implements Converter<List<UserFavouriteDto>, List<UserFavourite>>{

    @Override
    public List<UserFavouriteDto> convert(List<UserFavourite> from) {
        List<UserFavouriteDto> userFavouriteDtoList = new ArrayList<>();

        for (UserFavourite userFavourite: from) {
            UserFavouriteDto userFavouriteDto = UserFavouriteDto.builder()
                    .favouriteId(userFavourite.getUserFavouriteId())
                    .favouriteType(userFavourite.getFavouriteType())
                    .name(userFavourite.getName())
                    .build();

            userFavouriteDtoList.add(userFavouriteDto);
        }
        return userFavouriteDtoList;
    }
}