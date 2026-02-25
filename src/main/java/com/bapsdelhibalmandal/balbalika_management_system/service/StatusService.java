package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.model.Status;

import java.util.List;
import java.util.Optional;

public interface StatusService {
    Status saveStatus(Status status);
    Optional<Status> getStatusById(Integer id);
    List<Status> getAllStatuses();
    void deleteStatus(Integer id);
}
