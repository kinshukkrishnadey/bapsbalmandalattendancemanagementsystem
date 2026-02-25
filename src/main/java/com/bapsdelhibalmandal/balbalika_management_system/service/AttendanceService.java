package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.Attendance;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    Attendance markAttendance(Attendance attendance);
    Optional<Attendance> getAttendanceById(Long id);
    List<Attendance> getAllAttendances();
    List<Attendance> getAttendanceByKidId(Long kidId);
    void deleteAttendance(Long id);
}
