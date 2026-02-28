package com.bapsdelhibalmandal.balbalika_management_system.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KidUpdateDto {
    private String emailAddress;
    private String phoneNumber;
    private String houseNumber;
    private String area;
    private String state;
    private Long pincode;

    private Long sabhaKshetraId;           // Enrolled SabhaKshetra
    private Long assignedSabhaKshetraId;    // For Sanchalak/Sah-Sanchalak
    private Set<Long> supervisedSabhaKshetraIds;  // For Nirdeshak
    private Integer statusId;
    private Set<Integer> roleIds;
}
