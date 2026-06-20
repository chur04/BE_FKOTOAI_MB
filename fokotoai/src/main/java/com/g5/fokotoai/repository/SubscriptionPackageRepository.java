package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.SubscriptionPackage;
import com.g5.fokotoai.enums.PackageSubStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long> {

    List<SubscriptionPackage> findByStatus(PackageSubStatus status);
}
