package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.security.CustomUserDetails;
import xyz.mobi.testingautomationtool.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userDetails.getUser();
    }

}
