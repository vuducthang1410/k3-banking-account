package com.system.customer_service.repository;

import com.system.customer_service.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Province findByName(String name);

    @Query("SELECT p.number FROM Province p WHERE p.name = :name")
    String findNumberByName(@Param("name") String name);


}
