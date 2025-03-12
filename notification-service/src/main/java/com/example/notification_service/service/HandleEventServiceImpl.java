package com.example.notification_service.service;

import com.example.notification_service.domain.entity.BalanceFluctuation;
import com.example.notification_service.service.interfaces.*;
import com.system.common_library.dto.notifcation.BalanceFluctuationNotificationDTO;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HandleEventServiceImpl implements HandleEventService {
    private final SMSService smsService;
    private final EmailService emailService;
    private final SMSRegistationService smsRegistationService;
    private final SystemService systemService;
//
//    @DubboReference
//    private final CustomerDubboService customerDubboService;

    CustomerDetailDTO queryCustomerDetail(String customerCIF) {
//         return (CustomerDetailDTO) customerDubboService.getCustomerByCifCode(customerCIF); //process customerCIF to get customer detail
        return CustomerDetailDTO.builder()
                .cifCode(customerCIF)
                .phone("0703425730")
                .fullName("Pham Ngoc Anh Thu")
                .mail("chirido0807@gmail.com")
                .build();
    }
    @Override
    public boolean sendOTP(OTP otp, CustomerDetailDTO customer) {
        emailService.sendOTPAuthentication(otp, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(customer.getCifCode());
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            return smsService.sendOTPAuthentication(otp, customer.getPhone());
        }
        return false;
    }

    @Override
    public boolean sendOTP(OTP otp, String customerCIF) {
        CustomerDetailDTO customer = queryCustomerDetail(customerCIF);
        if(customer == null) {
            return false;
        }
        boolean emailSent = emailService.sendOTPAuthentication(otp, customer.getFullName(), customer.getMail());
        boolean smsSent = true;
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(customer.getCifCode());
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendWelcomeCustomerMessage(customer.getFullName(),customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendTransactionNotification(TransactionNotificationDTO transaction)  {
        CustomerDetailDTO customer = queryCustomerDetail(transaction.getCustomerCIF());
        if(customer == null) {
            return false;
        }
        boolean emailSent;
        boolean smsSent = true;
        if(transaction.isSuccess()){
            emailSent = emailService.sendTransactionSuccess(transaction, customer.getFullName(), customer.getMail());

            BalanceFluctuation balance = BalanceFluctuation.builder()
                    .balance(transaction.getBalance())
                    .accountNumber(transaction.getBeneficiaryAccount())
                    .customerCIF(customer.getCifCode())
                    .transactionAmount(transaction.getDebitAmount())
                    .transactionDate(transaction.getTransactionDate())
                    .content(transaction.getDetailsOfTransaction())
                    .build();
            systemService.saveBalanceFluctuation(balance);
            //search for sms registration
            boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(transaction.getCustomerCIF());
            //send sms to registered phone number
            if(customerRegisteredSMS) {
                smsSent = smsService.sendTransactionSuccess(transaction, customer.getPhone());
            }
        }
        else{
            emailSent = emailService.sendTransactionFail(transaction, customer.getFullName(), customer.getMail());
            boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(transaction.getCustomerCIF());
            //send sms to registered phone number
            if(customerRegisteredSMS) {
                smsSent = smsService.sendTransactionFail(transaction, customer.getPhone());
            }
        }
        return (emailSent || smsSent);
    }
    @Override
    public boolean sendBalanceFluctuation(BalanceFluctuationNotificationDTO balanceFluctuationNotificationDTO)  {
        BalanceFluctuation balanceFluctuation = BalanceFluctuation.builder()
                .customerCIF(balanceFluctuationNotificationDTO.getCustomerCIF())
                .accountNumber(balanceFluctuationNotificationDTO.getAccountNumber())
                .balance(balanceFluctuationNotificationDTO.getBalance())
                .content(balanceFluctuationNotificationDTO.getTransactionContent())
                .transactionAmount(balanceFluctuationNotificationDTO.getTransactionAmount())
                .transactionDate(balanceFluctuationNotificationDTO.getTransactionTime())
                .build();
        systemService.saveBalanceFluctuation(balanceFluctuation);
        return true;
    }
    @Override
    public boolean sendWelcomeCustomerMessage(CustomerDetailDTO customerDetailDTO) {
        boolean emailSent = emailService.sendWelcomeCustomerMessage(customerDetailDTO.getMail(),customerDetailDTO.getFullName(),customerDetailDTO.getPhone());
        boolean smsSent = true;
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(customerDetailDTO.getCifCode());
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendWelcomeCustomerMessage(customerDetailDTO.getFullName(),customerDetailDTO.getPhone());
        }
        return emailSent || smsSent;
    }
    @Override
    public boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious)  {
        //        CustomerDetailDTO customer = customerDubboService.getCustomerByCifCode(transaction.getCustomerCIF()); //process customerCIF to get customer detail
        CustomerDetailDTO customer = CustomerDetailDTO.builder()
                .cifCode(transactionSuspicious.getCustomerCIF())
                .phone("0703425730")
                .fullName("Pham Ngoc Anh Thu")
                .mail("chirido0807@gmail.com")
                .build();
        boolean emailSent = emailService.sendTransactionSuspicious(transactionSuspicious,customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(transactionSuspicious.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendTransactionSuspicious(transactionSuspicious,customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendUpdateCustomerInformation(CustomerDetailDTO customerDetail) {
        boolean emailSent = emailService.sendCUstomerUpdateInformation(customerDetail);
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(customerDetail.getCifCode());
        boolean smsSent = true;
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendCUstomerUpdateInformation(customerDetail);
        }
        return emailSent || smsSent;
    }
    @Override
    public boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti){
        CustomerDetailDTO customer  = queryCustomerDetail(loanAccountNoti.getCustomerCIF());
        boolean emailSent = emailService.sendAccountLoanRegisterSuccessful(loanAccountNoti, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanAccountNoti.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendAccountLoanRegisterSuccessful(loanAccountNoti, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendAccountSavingRegister(SavingAccountNoti savingAccount) {
        CustomerDetailDTO customer  = queryCustomerDetail(savingAccount.getCustomerCIF());
        boolean emailSent = emailService.sendAccountSavingRegisterSuccessful(savingAccount, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(savingAccount.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if(customerRegisteredSMS) {
            smsSent = smsService.sendAccountSavingRegisterSuccessful(savingAccount, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount) {
        CustomerDetailDTO customer = queryCustomerDetail(paymentAccount.getCustomerCIF());
        boolean emailSent = emailService.sendAccountPaymentRegisterSuccessful(paymentAccount, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(paymentAccount.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendAccountPaymentRegisterSuccessful(paymentAccount, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendLoanReminder(LoanReminderNoti loanReminder) {
        CustomerDetailDTO customer = queryCustomerDetail(loanReminder.getCustomerCIF());
        boolean emailSent = emailService.sendLoanReminder(loanReminder, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanReminder.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendLoanReminder(loanReminder, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail) {
        CustomerDetailDTO customer = queryCustomerDetail(loanFinancialReviewFail.getCustomerCIF());
        boolean emailSent = emailService.sendLoanFinancialReviewFail(loanFinancialReviewFail, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanFinancialReviewFail.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendLoanFinancialReviewFail(loanFinancialReviewFail, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess) {
        CustomerDetailDTO customer = queryCustomerDetail(loanFinancialReviewSuccess.getCustomerCIF());
        boolean emailSent = emailService.sendLoanFinancialReviewSuccess(loanFinancialReviewSuccess, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanFinancialReviewSuccess.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendLoanFinancialReviewSuccess(loanFinancialReviewSuccess, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendLoanCompletion(LoanCompletionNoti loanCompletion) {
        CustomerDetailDTO customer = queryCustomerDetail(loanCompletion.getCustomerCIF());
        boolean emailSent = emailService.sendLoanCompletion(loanCompletion, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanCompletion.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendLoanCompletion(loanCompletion, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendOverdueDept(LoanOverDueNoti loanOverDue) {
        CustomerDetailDTO customer = queryCustomerDetail(loanOverDue.getCustomerCIF());
        boolean emailSent = emailService.sendOverdueDept(loanOverDue, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanOverDue.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendOverdueDept(loanOverDue, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendDisbursementFail(String customerCIF) {
        CustomerDetailDTO customer = queryCustomerDetail(customerCIF);
        boolean emailSent = emailService.sendDisbursementFail(customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(customerCIF);
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendDisbursementFail(customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess) {
        CustomerDetailDTO customer = queryCustomerDetail(loandisbursementsuccess.getCustomerCIF());
        boolean emailSent = emailService.sendDisbursementSuccess(loandisbursementsuccess, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loandisbursementsuccess.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendDisbursementSuccess(loandisbursementsuccess, customer.getPhone());
        }
        return emailSent || smsSent;
    }

    @Override
    public boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti) {
        CustomerDetailDTO customer = queryCustomerDetail(loanPaymentSuccessNoti.getCustomerCIF());
        boolean emailSent = emailService.sendLoanPaymentSuccess(loanPaymentSuccessNoti, customer.getFullName(), customer.getMail());
        boolean customerRegisteredSMS = smsRegistationService.checkCustomerRegistration(loanPaymentSuccessNoti.getCustomerCIF());
        boolean smsSent = true;
        //send sms to registered phone number
        if (customerRegisteredSMS) {
            smsSent = smsService.sendLoanPaymentSuccess(loanPaymentSuccessNoti, customer.getPhone());
        }
        return emailSent || smsSent;
    }
}
