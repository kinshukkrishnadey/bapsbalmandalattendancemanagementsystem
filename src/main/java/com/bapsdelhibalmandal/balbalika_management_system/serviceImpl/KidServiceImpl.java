package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidUpdateDto;
import com.bapsdelhibalmandal.balbalika_management_system.mapper.KidMapper;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import com.bapsdelhibalmandal.balbalika_management_system.model.Role;
import com.bapsdelhibalmandal.balbalika_management_system.model.SabhaKshetra;
import com.bapsdelhibalmandal.balbalika_management_system.model.Status;
import com.bapsdelhibalmandal.balbalika_management_system.repository.KidRepository;
import com.bapsdelhibalmandal.balbalika_management_system.repository.SabhaKshetraRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.KidService;
import com.bapsdelhibalmandal.balbalika_management_system.util.GoogleCloudStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KidServiceImpl implements KidService {

    private final KidRepository kidRepository;
    private final SabhaKshetraRepository sabhaKshetraRepository;
    private final GoogleCloudStorageUtil googleCloudStorageUtil;
    private final KidMapper kidMapper;

    public KidServiceImpl(KidRepository kidRepository, SabhaKshetraRepository sabhaKshetraRepository,
                          GoogleCloudStorageUtil googleCloudStorageUtil, KidMapper kidMapper) {
        this.kidRepository = kidRepository;
        this.sabhaKshetraRepository = sabhaKshetraRepository;
        this.googleCloudStorageUtil = googleCloudStorageUtil;
        this.kidMapper = kidMapper;
    }

    private final String BUCKET_NAME = "your-gcs-bucket";
    private final String FOLDER_NAME = "kid-photos";


    @Override
    public Kid addKid(Kid kid, MultipartFile photo) {
        try {
            // Set registration date to current date if not already set
            if (kid.getRegistrationDate() == null) {
                kid.setRegistrationDate(LocalDate.now());
            }
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = uploadToGCS(photo);
                kid.setPhotoUrl(photoUrl);  // ✅ Fix: Set the photo URL to kid
            }
            return kidRepository.save(kid);
        } catch (Exception e) {
            throw new RuntimeException("Error saving kid with photo", e);
        }
    }

    @Override
    public List<Kid> listAllKids() {
        return kidRepository.findAll();
    }

    @Override
    public Kid getKidById(Long kidId) {
        return kidRepository.findById(kidId).orElseThrow(() -> new RuntimeException("Kid not found with ID: " + kidId));
    }

    @Override
    public Kid updateKid(Long id, KidUpdateDto updateDTO, MultipartFile photo) {
        Kid existingKid = kidRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kid not found with ID: " + id));
        kidMapper.updateKidFromDTO(updateDTO, existingKid);
        // ✅ Map SabhaKshetra manually if ID provided
        if (updateDTO.getSabhaKshetraId() != null) {
            SabhaKshetra sabhaKshetra = new SabhaKshetra();
            sabhaKshetra.setKshetraId(updateDTO.getSabhaKshetraId());
            existingKid.setSabhaKshetra(sabhaKshetra);
        }
        if (updateDTO.getAssignedSabhaKshetraId() != null) {
            SabhaKshetra assigned = new SabhaKshetra();
            assigned.setKshetraId(updateDTO.getAssignedSabhaKshetraId());
            existingKid.setAssignedSabhaKshetra(assigned);
        }
        if (updateDTO.getSupervisedSabhaKshetraIds() != null && !updateDTO.getSupervisedSabhaKshetraIds().isEmpty()) {
            Set<SabhaKshetra> supervised = new HashSet<>();
            for (Long kshetraId : updateDTO.getSupervisedSabhaKshetraIds()) {
                sabhaKshetraRepository.findById(kshetraId).ifPresent(supervised::add);
            }
            existingKid.setSupervisedSabhaKshetra(supervised);
        }

        // ✅ Map Status manually if ID provided
        if (updateDTO.getStatusId() != null) {
            Status status = new Status();
            status.setStatusId(updateDTO.getStatusId());
            existingKid.setStatus(status);
        }

        // ✅ Map Role Set if provided
        if (updateDTO.getRoleIds() != null && !updateDTO.getRoleIds().isEmpty()) {
            Set<Role> roles = updateDTO.getRoleIds().stream().map(roleId -> {
                Role role = new Role();
                role.setRoleId(roleId);
                return role;
            }).collect(Collectors.toSet());
            existingKid.setRoles(roles);
        }

        // ✅ Upload new photo if provided
        try {
            if (photo != null && !photo.isEmpty()) {
                File tempFile = File.createTempFile("photo-", photo.getOriginalFilename());
                photo.transferTo(tempFile);
                String url = googleCloudStorageUtil.uploadFile("your_bucket", "kids", photo.getOriginalFilename(), tempFile.toPath());
                existingKid.setPhotoUrl(url);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading photo to GCS", e);
        }

        return kidRepository.save(existingKid);
    }

    @Override
    public List<Kid> getKidsBySabhaKshetra(Long sabhaKshetraId) {
        return kidRepository.findBySabhaKshetra_KshetraId(sabhaKshetraId);
    }

    @Override
    public List<Kid> getKidsByRole(Integer roleId) {
        return kidRepository.findByRoleId(roleId);
    }

    // --- Utility method for converting MultipartFile to local temp file and uploading to GCS ---
    private String uploadToGCS(MultipartFile file) throws Exception {
        File convFile = File.createTempFile("upload-", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return googleCloudStorageUtil.uploadFile(BUCKET_NAME, FOLDER_NAME, file.getOriginalFilename(), Path.of(convFile.getAbsolutePath()));
    }
}
