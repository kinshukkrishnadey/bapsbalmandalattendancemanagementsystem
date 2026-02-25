package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.model.Attendance;
import com.bapsdelhibalmandal.balbalika_management_system.repository.AttendanceRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.AttendanceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }



    @Override
    public Attendance markAttendance(Attendance attendance) {
        // If no attendanceDate provided, set to today
        if (attendance.getAttendanceDate() == null) {
            attendance.setAttendanceDate(LocalDate.now());
        }

        // Check if attendance already exists for this kid on this date
        boolean exists = attendanceRepository.existsByKid_KidIdAndAttendanceDate(
                attendance.getKid().getKidId(),
                attendance.getAttendanceDate()
        );

        if (exists) {
            throw new IllegalStateException("Attendance already marked for this kid on " + attendance.getAttendanceDate());
        }

        return attendanceRepository.save(attendance);
    }

    @Override
    public Optional<Attendance> getAttendanceById(Long id) {

        return attendanceRepository.findById(id);
    }

    @Override
    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    @Override
    public List<Attendance> getAttendanceByKidId(Long kidId) {
        return attendanceRepository.findByKid_KidId(kidId);
    }

    @Override
    public void deleteAttendance(Long id) {

    }


}
