package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.AiMiniChallengeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiMiniChallengeSessionRepository extends JpaRepository<AiMiniChallengeSession, Long> {

    Optional<AiMiniChallengeSession> findBySessionIdAndStudentStudentId(Long sessionId, Long studentId) ;

    List<AiMiniChallengeSession> findTop5ByStudentStudentIdOrderByCreatedAtDesc(Long studentId) ;
}
