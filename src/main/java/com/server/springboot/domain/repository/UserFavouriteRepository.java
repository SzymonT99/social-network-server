package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.UserFavourite;
import com.server.springboot.domain.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavouriteRepository extends JpaRepository<UserFavourite, Long> {

    List<UserFavourite> findByUserProfile(UserProfile userProfiler);

}
