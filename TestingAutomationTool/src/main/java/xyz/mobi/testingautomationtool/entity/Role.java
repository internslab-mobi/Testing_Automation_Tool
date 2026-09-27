package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "testing_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_testing_roles_role",
                        columnNames = "role"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role", nullable = false, length = 100, unique = true)
    private String role;
}