package com.system.account_service.services;

// Class Service tính toán các loại lãi suất
public interface InterestCalcService {

    // Tính toán lãi suất tín dụng
    void calcCreditInterest(String accountId);

    // Tính toán lãi suất tiết kiệm
    void calcSavingInterest(String accountId);
}
