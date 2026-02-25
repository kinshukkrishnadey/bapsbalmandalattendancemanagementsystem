package com.bapsdelhibalmandal.balbalika_management_system.DTO;

import lombok.Data;

import java.util.Set;

@Data
public class UserDto {
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private boolean isKid;
    private Long kidId;
    private Set<String> roleNames;
}
