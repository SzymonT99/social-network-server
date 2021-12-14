package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.*;
import com.server.springboot.domain.entity.Post;
import com.server.springboot.domain.entity.SharedPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SharedPostDtoListMapper implements Converter<List<SharedPostDto>, List<SharedPost>> {

    private final Converter<PostDto, Post> postDtoMapper;

    @Autowired
    public SharedPostDtoListMapper(Converter<PostDto, Post> postDtoMapper) {
        this.postDtoMapper = postDtoMapper;
    }

    @Override
    public List<SharedPostDto> convert(List<SharedPost> from) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<SharedPostDto> sharedPostDtoList = new ArrayList<>();

        for (SharedPost sharedPost : from) {
            SharedPostDto sharedPostDto = SharedPostDto.builder()
                    .userId(sharedPost.getSharedPostUser().getUserId())
                    .authorOfSharing(sharedPost.getSharedPostUser().getUserProfile().getFirstName()
                            + " " + sharedPost.getSharedPostUser().getUserProfile().getLastName())
                    .sharingText(sharedPost.getNewPost().getText())
                    .sharingDate(sharedPost.getDate().format(formatter))
                    .isPublic(sharedPost.getNewPost().isPublic())
                    .sharedPost(postDtoMapper.convert(sharedPost.getBasePost()))
                    .build();
            sharedPostDtoList.add(sharedPostDto);
        }

        return sharedPostDtoList;
    }
}
