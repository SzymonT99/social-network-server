package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.UserAccountDto;
import com.server.springboot.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAccountDtoListMapper implements Converter<List<UserAccountDto>, List<User>> {

    @Override
    public List<UserAccountDto> convert(List<User> from) {
        List<UserAccountDto> userAccountDtoList = new ArrayList<>();
        for (User user : from) {
            UserAccountDto userAccountDto = UserAccountDto.builder()
                    .id(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getUserProfile().getFirstName())
                    .lastName(user.getUserProfile().getLastName())
                    .isPublicProfile(user.getUserProfile().isPublic())
                    .phoneNumber(user.getPhoneNumber())
                    .incorrectLoginCounter(user.getIncorrectLoginCounter())
                    .activateAccount(user.isVerifiedAccount())
                    .isBlocked(user.isBlocked())
                    .isBanned(user.isBanned())
                    .build();

            userAccountDtoList.add(userAccountDto);
        }

        return userAccountDtoList;
    }
}