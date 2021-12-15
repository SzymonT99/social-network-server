package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.EventMember;
import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.enumeration.EventParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

    List<EventMember> findByEventMemberAndParticipationStatus(User user, EventParticipationStatus eventParticipationStatus);

    Optional<EventMember> findByEventMemberAndEvent(User user, Event event);
}
