package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.GroupRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GroupRuleRepository extends JpaRepository<GroupRule, Long> {

    @Transactional
    void deleteByRuleId(Long ruleId);
}
