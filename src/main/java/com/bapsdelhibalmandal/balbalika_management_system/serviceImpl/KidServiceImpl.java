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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(KidServiceImpl.class);
    
    private final KidRepository kidRepository;
    private final SabhaKshetraRepository sabhaKshetraRepository;
    private final GoogleCloudStorageUtil googleCloudStorageUtil;
    private final KidMapper kidMapper;

    @Value("${gcp.bucket.name:baps-child-photos}")
    private String bucketName;

    private final String FOLDER_NAME = "kid-photos";

    public KidServiceImpl(KidRepository kidRepository, SabhaKshetraRepository sabhaKshetraRepository,
                          GoogleCloudStorageUtil googleCloudStorageUtil, KidMapper kidMapper) {
        this.kidRepository = kidRepository;
        this.sabhaKshetraRepository = sabhaKshetraRepository;
        this.googleCloudStorageUtil = googleCloudStorageUtil;
        this.kidMapper = kidMapper;
    }


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
            Kid savedKid = kidRepository.save(kid);
            // Generate signed URL for photo before returning
            generateSignedPhotoUrl(savedKid);
            return savedKid;
        } catch (Exception e) {
            throw new RuntimeException("Error saving kid with photo", e);
        }
    }

    @Override
    public List<Kid> listAllKids() {
        List<Kid> kids = kidRepository.findAll();
        // Generate signed URLs for all kids' photos
        kids.forEach(this::generateSignedPhotoUrl);
        return kids;
    }

    @Override
    public Kid getKidById(Long kidId) {
        Kid kid = kidRepository.findById(kidId).orElseThrow(() -> new RuntimeException("Kid not found with ID: " + kidId));
        // Generate signed URL for photo
        generateSignedPhotoUrl(kid);
        return kid;
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
                String url = googleCloudStorageUtil.uploadFile(bucketName, "kid-photos", photo.getOriginalFilename(), tempFile.toPath());
                existingKid.setPhotoUrl(url);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading photo to GCS", e);
        }

        Kid updatedKid = kidRepository.save(existingKid);
        // Generate signed URL for photo before returning
        generateSignedPhotoUrl(updatedKid);
        return updatedKid;
    }

    @Override
    public List<Kid> getKidsBySabhaKshetra(Long sabhaKshetraId) {
        List<Kid> kids = kidRepository.findBySabhaKshetra_KshetraId(sabhaKshetraId);
        // Generate signed URLs for all kids' photos
        kids.forEach(this::generateSignedPhotoUrl);
        return kids;
    }

    @Override
    public List<Kid> getKidsByRole(Integer roleId) {
        List<Kid> kids = kidRepository.findByRoleId(roleId);
        // Generate signed URLs for all kids' photos
        kids.forEach(this::generateSignedPhotoUrl);
        return kids;
    }

    // --- Utility method for converting MultipartFile to local temp file and uploading to GCS ---
    private String uploadToGCS(MultipartFile file) throws Exception {
        File convFile = File.createTempFile("upload-", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return googleCloudStorageUtil.uploadFile(bucketName, FOLDER_NAME, file.getOriginalFilename(), Path.of(convFile.getAbsolutePath()));
    }

    /**
     * Generates a signed URL for the kid's photo if it exists.
     * This allows secure access to private GCS objects without making the bucket public.
     */
    private void generateSignedPhotoUrl(Kid kid) {
        if (kid != null && kid.getPhotoUrl() != null && !kid.getPhotoUrl().isEmpty()) {
            try {
                String originalUrl = kid.getPhotoUrl();
                String signedUrl = googleCloudStorageUtil.generateSignedUrl(originalUrl);
                if (signedUrl != null && !signedUrl.equals(originalUrl)) {
                    kid.setPhotoUrl(signedUrl);
                    logger.debug("Generated signed URL for kid {} photo", kid.getKidId());
                } else {
                    logger.warn("Signed URL generation returned original URL for kid {} photo: {}", kid.getKidId(), originalUrl);
                }
            } catch (Exception e) {
                // If signed URL generation fails, keep original URL
                // Log error but don't fail the request
                logger.error("Failed to generate signed URL for kid {} photo: {} - {}", kid.getKidId(), kid.getPhotoUrl(), e.getMessage(), e);
            }
        }
    }
}
