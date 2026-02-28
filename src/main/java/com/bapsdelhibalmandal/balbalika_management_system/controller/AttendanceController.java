package com.bapsdelhibalmandal.balbalika_management_system.controller;


import com.bapsdelhibalmandal.balbalika_management_system.Exception.DuplicateAttendanceException;
import com.bapsdelhibalmandal.balbalika_management_system.model.Attendance;
import com.bapsdelhibalmandal.balbalika_management_system.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

        private final AttendanceService attendanceService;

        @Autowired
        public AttendanceController(AttendanceService attendanceService) {
            this.attendanceService = attendanceService;
        }

        // ✅ 1. Mark attendance
        @PostMapping
        public ResponseEntity<?> markAttendance(@RequestBody Attendance attendance) {
            try {
                Attendance saved = attendanceService.markAttendance(attendance);
                return ResponseEntity.ok(saved);
            } catch (DuplicateAttendanceException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }

        // ✅ 2. Get attendance by ID
        @GetMapping("/{id}")
        public ResponseEntity<?> getAttendanceById(@PathVariable Long id) {
            Optional<Attendance> attendance = attendanceService.getAttendanceById(id);
            return attendance.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // ✅ 3. Get all attendances
        @GetMapping
        public ResponseEntity<List<Attendance>> getAllAttendances() {
            return ResponseEntity.ok(attendanceService.getAllAttendances());
        }

        // ✅ 4. Get attendance by kid ID
        @GetMapping("/kid/{kidId}")
        public ResponseEntity<List<Attendance>> getAttendanceByKidId(@PathVariable Long kidId) {
            return ResponseEntity.ok(attendanceService.getAttendanceByKidId(kidId));
        }

        // ✅ 5. Delete attendance by ID
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
            attendanceService.deleteAttendance(id);
            return ResponseEntity.ok("Attendance deleted successfully.");
        }
}
