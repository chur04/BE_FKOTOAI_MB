package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student , Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Student> findById(Long id);

    Optional<Student> findByEmail(String email ) ;

    List<Student> findTop10ByOrderByRankPointsDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) + 1 FROM Student s WHERE s.rankPoints > :rankPoints")
    int calculateNationalRank(@org.springframework.data.repository.query.Param("rankPoints") Integer rankPoints);

    List<Student> findAllByEmail(String email);
}
