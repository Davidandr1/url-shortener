package com.davidandriychuk.url_shortener.service;

import com.davidandriychuk.url_shortener.security.CustomUserDetails;
import com.davidandriychuk.url_shortener.entity.User;
import com.davidandriychuk.url_shortener.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository){
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found" + email));
        return new CustomUserDetails(user);
    }

}