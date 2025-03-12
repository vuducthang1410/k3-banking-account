package com.system.account_service.repositories;

import com.system.account_service.entities.CreditProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditProfileRepository extends JpaRepository<CreditProfiles, String> {

}
