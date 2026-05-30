package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findTop10ByOrderByRankPointsDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) + 1 FROM Student s WHERE s.rankPoints > :rankPoints")
    int calculateNationalRank(@org.springframework.data.repository.query.Param("rankPoints") Integer rankPoints);
}
