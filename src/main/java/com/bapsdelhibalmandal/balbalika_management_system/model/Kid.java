package com.bapsdelhibalmandal.balbalika_management_system.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
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
    private String gender;

    @DateTimeFormat(pattern = "ddMMyyyy")
    private LocalDate dateOfBirth;


    private Integer age;
    private String motherName;
    private String fatherName;
    private String houseNumber;
    private String area;
    private String state;
    private Long pincode;
    private String phoneNumber;
    private String emailAddress;


    @OneToOne
    @JoinTable(
            name = "kid_sabhakshetra",
            joinColumns = @JoinColumn(name = "kid_id"),
            inverseJoinColumns = @JoinColumn(name = "sabhakshetra_id")
    )
    private SabhaKshetra sabhaKshetra;

    @OneToOne
    @JoinTable(
            name = "kid_status",
            joinColumns = @JoinColumn(name = "kid_id"),
            inverseJoinColumns = @JoinColumn(name = "status_id")
    )
    private Status status;

    @ManyToMany
    @JoinTable(
            name = "kid_role",
            joinColumns = @JoinColumn(name = "kid_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
