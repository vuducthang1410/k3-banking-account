package org.demo.loanservice.services.backgoundService;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.DateUtil;
import org.demo.loanservice.dto.enumDto.RequestStatus;
import org.demo.loanservice.entities.FinancialInfo;
import org.demo.loanservice.repositories.FinancialInfoRepository;
import org.demo.loanservice.services.impl.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialInfoScheduleService {
    private final FinancialInfoRepository financialInfoRepository;
    private final Logger log = LogManager.getLogger(FinancialInfoScheduleService.class);
    private final RedisService redisService;

    @Scheduled(cron = "0 0 0 * * ?")
    private void automaticallySetExpiredForFinancialInfoExpired() {
        Date currentDate = Date.valueOf(DateUtil.convertTimeStampToLocalDate(DateUtil.getCurrentTimeUTC7()));
        List<FinancialInfo> financialInfoList = financialInfoRepository
                .findAllByIsDeletedFalseAndIsExpiredFalseAndExpiredDateAfterAndRequestStatusIn(
                        currentDate,
                        List.of(
                                RequestStatus.APPROVED,
                                RequestStatus.REJECTED
                        )
                );
        log.info("Financial Infos is expired: {}", financialInfoList.size());
        financialInfoList.forEach(financialInfo -> {
            financialInfo.setIsExpired(true);
            if (financialInfo.getRequestStatus() == RequestStatus.PENDING) {
                financialInfo.setRequestStatus(RequestStatus.EXPIRED);
            }
            financialInfoRepository.save(financialInfo);
            redisService.deleteCacheFinancialInfoDetailById(financialInfo.getId());
        });
    }
}
