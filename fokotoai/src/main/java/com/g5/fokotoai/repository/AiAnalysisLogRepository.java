package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.AiAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiAnalysisLogRepository extends JpaRepository<AiAnalysisLog, Long> {


    List<AiAnalysisLog> findTop20ByStudentStudentIdOrderByCreatedAtDesc(Long studentId) ;
}
