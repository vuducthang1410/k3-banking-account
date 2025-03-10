package com.system.napas_service.init;

import com.system.napas_service.dto.response.BankApiDTO;
import com.system.napas_service.entity.Account;
import com.system.napas_service.mapper.BankApiMapper;
import com.system.napas_service.repository.AccountRepository;
import com.system.napas_service.repository.BankRepository;
import de.huxhorn.sulky.ulid.ULID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RestTemplate restTemplate;

    private final BankApiMapper bankApiMapper;

    private final BankRepository bankRepository;

    private final AccountRepository accountRepository;

    @Value("${url.bank.api}")
    private String URL;

    @Override
    public void run(String... args) {

        try {

            if (bankRepository.count() <= 0) {

                ResponseEntity<?> rs = restTemplate.getForEntity(URL, BankApiDTO.class);

                if (rs.getStatusCode().equals(HttpStatus.OK)) {

                    BankApiDTO dto = (BankApiDTO) Objects.requireNonNull(rs.getBody());

                    bankRepository.saveAll(dto.getData().stream()
                            .map(bankApiMapper::responseToEntity).toList());

                    log.info("Database initialized: Add {} banks", dto.getData().size());
                }
            }
        } catch (Exception ignore) {

        }

        try {

            if (accountRepository.count() <= 0) {

                accountRepository.save(Account.builder()
                        .id(new ULID().nextULID())
                        .bank(bankRepository.findAll().stream().findFirst().orElse(null))
                        .accountId("NapasAccountId")
                        .accountNumber("NapasAccountNumber")
                        .balance(BigDecimal.TEN)
                        .availableBalance(BigDecimal.TEN)
                        .totalIncome(BigDecimal.TEN)
                        .totalExpenditure(BigDecimal.ZERO)
                        .customerName("PQT")
                        .isActive(true)
                        .lastTransactionDate(null)
                        .description("description")
                        .state(true)
                        .status(true)
                        .build());
            }
        } catch (Exception ignore) {

        }
    }
}
