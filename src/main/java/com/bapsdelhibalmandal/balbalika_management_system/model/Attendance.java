package com.bapsdelhibalmandal.balbalika_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @DateTimeFormat(pattern = "ddMMyyyy")
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    public enum AttendanceStatus{
        PRESENT, ABSENT

    }


    @ManyToOne
    @JoinColumn(name = "kidId")
    private Kid kid;
}



