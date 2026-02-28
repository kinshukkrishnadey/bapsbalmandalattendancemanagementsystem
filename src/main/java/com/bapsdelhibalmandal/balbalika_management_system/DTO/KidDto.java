package com.bapsdelhibalmandal.balbalika_management_system.DTO;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Gender;
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
    private Gender gender;  // BAL = Male, BALIKA = Female
    private LocalDate dateOfBirth;
    private LocalDate registrationDate;  // Date when kid was registered
    private Integer age;
    private String motherName;
    private String photoUrl;
    private String phoneNumber;
    private String area;

    // Related IDs
    private Long sabhaKshetraId;           // Where kid is enrolled (student)
    private Long assignedSabhaKshetraId;   // Where Sanchalak/Sah-Sanchalak takes attendance
    private Set<Long> supervisedSabhaKshetraIds;  // SabhaKshetra Nirdeshak oversees
    private Integer statusId;
    private Set<Integer> roleIds;
}
