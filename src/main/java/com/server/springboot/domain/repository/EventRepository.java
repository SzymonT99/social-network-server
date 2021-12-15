package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Transactional
    void deleteByEventId(Long eventId);

    List<Event> findByIsDeletedOrderByCreatedAtDesc(boolean isDeleted);
}
