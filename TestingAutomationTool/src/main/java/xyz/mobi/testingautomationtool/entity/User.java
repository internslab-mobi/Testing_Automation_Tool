package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.audit.Auditable;
import xyz.mobi.testingautomationtool.enums.Role;

@Entity
@Table(
        name = "testing_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "testing_users_username_unique",
                        columnNames = "username"
                ),
                @UniqueConstraint(
                        name = "testing_users_email_unique",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "role", nullable = false, length = 255)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}