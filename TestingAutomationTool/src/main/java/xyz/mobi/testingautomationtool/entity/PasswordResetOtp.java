package xyz.mobi.testingautomationtool.entity;

import jakarta.persistence.*;
import lombok.*;
import xyz.mobi.testingautomationtool.audit.Auditable;

import java.time.Instant;

@Entity
@Table(
        name = "testing_password_reset_otps",
        indexes = {
                @Index(name = "idx_testing_otps_email", columnList = "email"),
                @Index(name = "idx_testing_otps_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetOtp extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Integer otpId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_testing_password_reset_otps_user")
    )
    private User user;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryTime);
    }
}
