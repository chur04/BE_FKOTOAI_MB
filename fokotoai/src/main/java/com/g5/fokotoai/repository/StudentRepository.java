package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student , Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Student> findById(Long id);

    Optional<Student> findByEmail(String email ) ;

}
