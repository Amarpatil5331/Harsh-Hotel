package com.hotel.services.jwt;



import com.hotel.entity.AppUser;
import com.hotel.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> user = userRepository.getMyUserByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("No User Found!");
        }
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.get().getRole().getRoleName());
        return new User(user.get().getUsername(), user.get().getPassword(), Collections.singletonList(authority));
    }

}