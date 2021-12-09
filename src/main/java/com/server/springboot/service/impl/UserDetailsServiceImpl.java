package com.server.springboot.service.impl;

import com.server.springboot.domain.entity.User;
import com.server.springboot.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher emailMatcher = pattern.matcher(login);
        User user;

        if (emailMatcher.matches()) {
            user = userRepository.findByEmail(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Not Found user with this email: " + login));
        } else {
            user = userRepository.findByUsername(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Not Found user with this username: " + login));
        }

        return UserDetailsImpl.build(user);
    }

}