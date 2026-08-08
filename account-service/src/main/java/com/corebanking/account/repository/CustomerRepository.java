package com.corebanking.account.repository;

import com.corebanking.account.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdentityNumber(String identityNumber);
}
