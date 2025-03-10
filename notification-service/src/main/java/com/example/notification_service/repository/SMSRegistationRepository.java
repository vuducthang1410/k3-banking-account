package com.example.notification_service.repository;

import com.example.notification_service.domain.entity.SMSRegistration;
import org.springframework.data.repository.CrudRepository;

public interface SMSRegistationRepository extends CrudRepository<SMSRegistration, String> {

}
