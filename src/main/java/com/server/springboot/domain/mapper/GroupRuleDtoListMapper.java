package com.server.springboot.domain.mapper;

import com.server.springboot.domain.dto.response.GroupRuleDto;
import com.server.springboot.domain.entity.GroupRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupRuleDtoListMapper implements Converter<List<GroupRuleDto>, List<GroupRule>> {

    @Override
    public List<GroupRuleDto> convert(List<GroupRule> from) {
        List<GroupRuleDto> groupRuleDtoList = new ArrayList<>();

        for (GroupRule groupRule : from) {
            GroupRuleDto groupRuleDto = GroupRuleDto.builder()
                    .ruleId(groupRule.getRuleId())
                    .name(groupRule.getName())
                    .description(groupRule.getDescription())
                    .build();

            groupRuleDtoList.add(groupRuleDto);
        }

        return groupRuleDtoList;
    }
}
