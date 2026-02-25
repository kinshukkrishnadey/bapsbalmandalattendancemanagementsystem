package com.bapsdelhibalmandal.balbalika_management_system.model;

import com.bapsdelhibalmandal.balbalika_management_system.enums.Right;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer roleId;
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    @ElementCollection(targetClass = Right.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "role_rights", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "role_right")
    private Set<Right> rights;

}
