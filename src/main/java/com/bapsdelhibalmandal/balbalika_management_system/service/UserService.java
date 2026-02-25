package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> getUserByPhoneNumber(String phoneNumber);
    List<User> getAllUsers();
}
