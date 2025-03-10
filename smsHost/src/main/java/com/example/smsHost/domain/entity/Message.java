package com.example.smsHost.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "message", nullable = false ,columnDefinition = "LONGTEXT")
    private String message;
    @Column(name = "sender", nullable = false, length = 20)
    private String sender;
    @Column(name = "receiver", nullable = false, length = 20)
    private String receiver;
    private LocalDateTime sendTime;
}
