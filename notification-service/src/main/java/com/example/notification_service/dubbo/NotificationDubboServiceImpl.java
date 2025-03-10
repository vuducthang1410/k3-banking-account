package com.example.notification_service.dubbo;

import com.example.notification_service.service.interfaces.EmailService;
import com.example.notification_service.service.interfaces.HandleEventService;
import com.example.notification_service.service.interfaces.SMSService;
import com.system.common_library.dto.notifcation.BalanceFluctuationNotificationDTO;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.service.NotificationDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class NotificationDubboServiceImpl implements NotificationDubboService {
//    private final Validator validator;
    private final EmailService emailService;
    private final HandleEventService handleEventServiceImpl;
    private final SMSService smsService;


    @Override
    public boolean sendOtpCodeTransaction(OTP otp, String customerCIF) {
        return handleEventServiceImpl.sendOTP(otp,customerCIF);

    }

    @Override
    public boolean sendOtpCodeCustomerRegistration(OTP otp, String phoneNumber) {
        return smsService.sendOTPAuthentication(otp, phoneNumber);
    }

    @Override
    public boolean sendOtpCodeEmailCustomerRegistration(OTP otp, String email, String customerFullname) {
        return emailService.sendOTPAuthentication(otp,customerFullname, email);
    }

    @Override
    public boolean sendOTPCodeCustomerResetPassword(OTP otp, CustomerDetailDTO customerDetail) {
        return handleEventServiceImpl.sendOTP(otp,customerDetail);
    }


    @Override
    public boolean sendTransactionNotification(TransactionNotificationDTO data){
        return handleEventServiceImpl.sendTransactionNotification(data);
    }

    @Override
    public boolean sendBalanceFluctuation(BalanceFluctuationNotificationDTO balanceFluctuation) {
        return handleEventServiceImpl.sendBalanceFluctuation(balanceFluctuation);
    }
}
