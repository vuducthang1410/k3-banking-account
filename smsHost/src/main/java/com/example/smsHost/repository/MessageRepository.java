package com.example.smsHost.repository;

import com.example.smsHost.domain.entity.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface MessageRepository extends CrudRepository<Message, Integer> {
}
