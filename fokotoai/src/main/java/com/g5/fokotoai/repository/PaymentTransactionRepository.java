package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.PaymentTransaction;
import com.g5.fokotoai.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByVnpayTxnRef(String vnpayTxnRef);

    boolean existsByVnpayTxnRef(String vnpayTxnRef);

    Optional<PaymentTransaction> findTopByStudentStudentIdAndStatusOrderByCreatedAtDesc(
            Long studentId, TransactionStatus status);
}
