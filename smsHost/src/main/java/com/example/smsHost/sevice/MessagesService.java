package com.example.smsHost.sevice;

import com.example.smsHost.domain.dto.MessageDTO;
import com.example.smsHost.domain.dto.MessageRequestDTO;
import com.example.smsHost.domain.entity.Message;
import com.example.smsHost.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class MessagesService {
    private  final MessageRepository messageRepository;

    public MessagesService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageDTO saveMessage(MessageRequestDTO message) {
        Message messageEntity = Message.builder()
                .message(message.getMessage())
                .sender(message.getSenderPhonenumber())
                .receiver(message.getReceiverPhonenumber())
                .sendTime(LocalDateTime.now())
                .build();
        Message savedMessage = messageRepository.save(messageEntity);
        return castMessageToDTO(savedMessage);
    }
    public List<MessageDTO> getSentMessages() {
        Iterable<Message> messages = messageRepository.findAll();

        List<MessageDTO> messageDTOS = new ArrayList<>();
        for (Message message : messages) {
            messageDTOS.add(castMessageToDTO(message));
        }
        return messageDTOS;
    }
    public MessageDTO castMessageToDTO(Message message) {
        return MessageDTO.builder()
                .message(message.getMessage())
                .senderPhonenumber(message.getSender())
                .receiverPhonenumber(message.getReceiver())
                .sendTime(message.getSendTime())
                .build();
    }
}
