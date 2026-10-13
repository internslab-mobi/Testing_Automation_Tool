package xyz.mobi.testingautomationtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.mobi.testingautomationtool.entity.PasswordResetOtp;
import xyz.mobi.testingautomationtool.entity.User;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Integer> {

    Optional<PasswordResetOtp> findByEmail(String email);

    Optional<PasswordResetOtp> findByResetToken(String resetToken);

    Optional<PasswordResetOtp> findByUser(User user);

    Optional<PasswordResetOtp> findByUser_UserId(Integer userId);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.user.userId = :userId")
    int deleteByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.email = :email")
    int deleteByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.user = :user")
    int deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM PasswordResetOtp o WHERE o.resetToken = :resetToken")
    int deleteByResetToken(@Param("resetToken") String resetToken);
}
