package com.bapsdelhibalmandal.balbalika_management_system.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer statusId;
    private String status;


    @OneToOne(mappedBy = "status")
    private Kid kid;

}
