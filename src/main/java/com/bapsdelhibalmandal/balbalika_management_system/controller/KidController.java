package com.bapsdelhibalmandal.balbalika_management_system.controller;

import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidDto;
import com.bapsdelhibalmandal.balbalika_management_system.DTO.KidUpdateDto;
import com.bapsdelhibalmandal.balbalika_management_system.mapper.KidMapper;
import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import com.bapsdelhibalmandal.balbalika_management_system.service.KidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/kid")
public class KidController {

    @Autowired
    private KidService kidService;

    @Autowired
    private KidMapper kidMapper;

    @PreAuthorize("hasPermission(null, 'ADD_KID')")
    @PostMapping("/register")
    public ResponseEntity<Kid> addKid(@RequestPart("kid") Kid kid,
                                      @RequestPart(value = "photo", required = false) MultipartFile photo){
        return new ResponseEntity<Kid>(kidService.addKid(kid, photo), HttpStatus.CREATED);

    }

    @GetMapping("/getallkiddetails")
    public ResponseEntity<List<Kid>> getAllKidWithAllDetails(){
        return new ResponseEntity<List<Kid>>(kidService.listAllKids(), HttpStatus.OK);
    }

    @GetMapping("/getkiddetails/{id}")
    public ResponseEntity<Kid> getKidById(@PathVariable("id") Long kidId){
        return new ResponseEntity<Kid>(kidService.getKidById(kidId), HttpStatus.OK);

    }

    @PreAuthorize("hasPermission(null, 'UPDATE_KID')")
    @PutMapping("/update/{id}")
    public ResponseEntity<KidDto> updateKid(@PathVariable("id") Long kidId,
                                            @RequestPart("updateDTO") KidUpdateDto dto,
                                            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        Kid updated = kidService.updateKid(kidId, dto, photo);
        return ResponseEntity.ok(kidMapper.toDto(updated));
    }


    @GetMapping("/getallkidpartialdetails")
    public ResponseEntity<List<KidDto>> getAllKids() {
        List<Kid> kids = kidService.listAllKids();
        List<KidDto> kidDTOs = kids.stream().map(kidMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(kidDTOs);
    }


    // ✅ Get one kid by ID

    @GetMapping("/getkidpartialdetails/{id}")
    public ResponseEntity<KidDto> getPartialKidById(@PathVariable("id") Long kidId) {
        Kid kid = kidService.getKidById(kidId);
        return ResponseEntity.ok(kidMapper.toDto(kid));
    }


    @GetMapping("/by-sabhakshetra/{sabhaKshetraId}")
    public ResponseEntity<List<KidDto>> getKidsBySabhaKshetra(@PathVariable Integer sabhaKshetraId) {
        List<Kid> kids = kidService.getKidsBySabhaKshetra(sabhaKshetraId);
        List<KidDto> dtoList = kids.stream().map(kidMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<List<KidDto>> getKidsByRole(@PathVariable Integer roleId) {
        List<Kid> kids = kidService.getKidsByRole(roleId);
        List<KidDto> dtoList = kids.stream().map(kidMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }


}
