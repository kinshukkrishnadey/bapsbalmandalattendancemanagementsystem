package com.bapsdelhibalmandal.balbalika_management_system.util;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Right;
import com.bapsdelhibalmandal.balbalika_management_system.model.User;

public class AuthUtil {
    public static boolean userHasRight(User user, Right right) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getRights().contains(right));
    }
}
