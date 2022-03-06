package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.request.RequestGroupRuleDto;
import com.server.springboot.domain.entity.GroupRule;
import org.springframework.stereotype.Component;

@Component
public class GroupRuleMapper implements Converter<GroupRule, RequestGroupRuleDto> {

    @Override
    public GroupRule convert(RequestGroupRuleDto from) {
        return GroupRule.builder()
                .name(from.getName())
                .description(from.getDescription())
                .build();
    }
}
