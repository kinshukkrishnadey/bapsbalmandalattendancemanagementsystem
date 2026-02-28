package com.bapsdelhibalmandal.balbalika_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "sabha_kshetra", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"zone_id", "kshetra_name"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SabhaKshetra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kshetraId;
    private String kshetraName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @OneToMany(mappedBy = "sabhaKshetra")
    @JsonIgnore
    private List<Kid> enrolledKids;

    @OneToMany(mappedBy = "assignedSabhaKshetra")
    @JsonIgnore
    private List<Kid> sanchalaks;
}
