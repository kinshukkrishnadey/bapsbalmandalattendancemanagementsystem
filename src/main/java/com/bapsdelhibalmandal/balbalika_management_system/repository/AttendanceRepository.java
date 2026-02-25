package com.bapsdelhibalmandal.balbalika_management_system.repository;

import com.bapsdelhibalmandal.balbalika_management_system.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByKid_KidId(Long kidId);
    boolean existsByKid_KidIdAndAttendanceDate(Long kidId, LocalDate attendanceDate);
}
