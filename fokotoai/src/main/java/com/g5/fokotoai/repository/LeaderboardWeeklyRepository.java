package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.LeaderboardWeekly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardWeeklyRepository extends JpaRepository<LeaderboardWeekly, Long> {
    List<LeaderboardWeekly> findTop10ByOrderByWeeklyPointsDesc();

    Optional<LeaderboardWeekly> findFirstByStudentStudentIdOrderByWeekStartDesc(Long studentId);
}
