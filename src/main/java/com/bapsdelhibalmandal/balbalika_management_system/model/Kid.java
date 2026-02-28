package com.bapsdelhibalmandal.balbalika_management_system.model;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Kid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kidId;
    private String firstName;
    private String lastName;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;  // BAL = Male, BALIKA = Female

    @DateTimeFormat(pattern = "ddMMyyyy")
    private LocalDate dateOfBirth;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;  // Date when kid was registered in the system

    private Integer age;
    private String motherName;
    private String fatherName;
    private String houseNumber;
    private String area;
    private String state;
    private Long pincode;
    private String phoneNumber;
    private String emailAddress;

    /** SabhaKshetra where kid is enrolled as student */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sabha_kshetra_id")
    private SabhaKshetra sabhaKshetra;

    /** Status: Active, Inactive */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    /** SabhaKshetra where kid (Sanchalak/Sah-Sanchalak) takes attendance */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sabha_kshetra_id")
    private SabhaKshetra assignedSabhaKshetra;

    /** SabhaKshetra that kid (Nirdeshak) oversees */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "kid_supervised_sabha_kshetra",
            joinColumns = @JoinColumn(name = "kid_id"),
            inverseJoinColumns = @JoinColumn(name = "sabha_kshetra_id")
    )
    private Set<SabhaKshetra> supervisedSabhaKshetra;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "kid_role",
            joinColumns = @JoinColumn(name = "kid_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
