package com.system.customer_service.repository;

import com.system.customer_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    boolean existsByPhone(String phone);

    boolean existsByIdentityCard(String identityCard);
    boolean existsByMail(String email);

    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByUserId(String userId);
    Optional<Customer> findCustomerById(String id);
    Optional<Customer> findCustomerByCifCode(String cifCode);
    Optional<Customer> findByMail(String mail);
    List<Customer> findByCifCodeIn(List<String> cifCode);
    List<Customer> findByIdIn(List<String> id);
    List<Customer> findByFirstNameContaining(String firstName);
    List<Customer> findByAddressContaining(String address);
    List<Customer> findByFirstNameContainingAndAddressContaining(String firstName, String address);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:firstName IS NULL OR c.firstName LIKE %:firstName%) AND " +
            "(:address IS NULL OR c.address LIKE %:address%)")
    List<Customer> findCustomers(@Param("firstName") String firstName,
                                 @Param("address") String address);
}
