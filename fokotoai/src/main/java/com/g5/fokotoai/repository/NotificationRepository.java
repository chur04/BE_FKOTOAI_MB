package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByStudentStudentIdAndIsReadFalse(Long studentId);
}
