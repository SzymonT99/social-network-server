package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Event;
import com.server.springboot.domain.entity.SharedEvent;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedEventRepository extends JpaRepository<SharedEvent, Long> {

    Optional<SharedEvent> findBySharedEventUserAndEvent(User user, Event event);

    List<SharedEvent> findBySharedEventUser(User user);
}