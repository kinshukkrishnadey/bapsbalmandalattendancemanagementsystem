package com.bapsdelhibalmandal.balbalika_management_system.Security;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Right;
import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        String phone = authentication.getName();
        Right right = Right.valueOf(permission.toString());

        User user = userRepository.findByPhoneNumber(phone)
                .orElse(null);

        if (user == null) return false;

        return user.getRoles().stream()
                .anyMatch(role -> role.getRights().contains(right));
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}
