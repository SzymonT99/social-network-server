package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.CreateUserDto;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.entity.UserProfile;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class UserMapper implements Converter<User, CreateUserDto>{

    @Override
    public User convert(CreateUserDto from) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        UserProfile userProfile = UserProfile.builder()
                .isPublic(true)
                .firstName(from.getFirstName())
                .lastName(from.getLastName())
                .gender(from.getGender())
                .dateOfBirth(LocalDate.parse(from.getDateOfBirth(), formatter))
                .age(LocalDate.now().getYear() - LocalDate.parse(from.getDateOfBirth(), formatter).getYear())
                .build();

        return User.builder()
                .role(Role.ROLE_USER)
                .username(from.getUsername())
                .password(from.getPassword())
                .email(from.getEmail())
                .phoneNumber(from.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .verifiedAccount(false)
                .activityStatus(ActivityStatus.OFFLINE)
                .isBlocked(false)
                .isDeleted(false)
                .userProfile(userProfile)
                .build();
    }

}

