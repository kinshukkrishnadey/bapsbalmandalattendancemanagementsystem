package com.bapsdelhibalmandal.balbalika_management_system.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KidDto {
    private String gender;
    private LocalDate dateOfBirth;
    private Integer age;
    private String motherName;

    private String photoUrl;

    // Contact Info
    private String phoneNumber;

    // Address Info
    private String area;

    // Related IDs for dropdowns
    private Integer sabhaKshetraId;
    private Integer statusId;
    private Set<Integer> roleIds;
}
