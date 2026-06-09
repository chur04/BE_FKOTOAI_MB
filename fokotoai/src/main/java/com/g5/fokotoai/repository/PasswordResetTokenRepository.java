package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.PasswordResetToken;
import com.g5.fokotoai.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken , Long> {

    Optional<PasswordResetToken> findByStudentAndTokenAndIsUsedFalse(Student student, String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p " + "WHERE p.student = :student AND p.isUsed = false")
    void deleteByStudentAndIsUsedFalse(Student student);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteByExpiresAtBefore(Instant now);

}
