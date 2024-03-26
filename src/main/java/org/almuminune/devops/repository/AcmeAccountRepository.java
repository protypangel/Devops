package org.almuminune.devops.repository;

import org.almuminune.devops.data.AcmeAccountData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcmeAccountRepository extends JpaRepository<AcmeAccountData, Long> {
}
