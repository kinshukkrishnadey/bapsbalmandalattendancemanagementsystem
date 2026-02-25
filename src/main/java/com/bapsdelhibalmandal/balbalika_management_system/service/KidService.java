package com.bapsdelhibalmandal.balbalika_management_system.service;

import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidUpdateDto;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KidService {

    public Kid addKid(Kid kid, MultipartFile photo);
    public List<Kid> listAllKids();
    public Kid getKidById(Long kidId);
    Kid updateKid(Long id, KidUpdateDto updateDTO, MultipartFile photo);
    List<Kid> getKidsBySabhaKshetra(Integer sabhaKshetraId);
    List<Kid> getKidsByRole(Integer roleId);

}
