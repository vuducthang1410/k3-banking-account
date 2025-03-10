package com.system.transaction_service.service.interfaces;

import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.transaction_service.dto.request.OTPRequestDTO;

public interface NotificationService {

    void sendOtpCode(OTPRequestDTO request);

    void sendTransactionNotification(TransactionNotificationDTO data);
}
