package com.system.transaction_service.init;

import com.system.transaction_service.dto.response.BankApiDTO;
import com.system.transaction_service.mapper.BankApiMapper;
import com.system.transaction_service.repository.ExternalBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RestTemplate restTemplate;

    private final BankApiMapper bankApiMapper;

    private final ExternalBankRepository externalBankRepository;

    @Value("${url.bank.api}")
    private String URL;

    @Override
    public void run(String... args) {

        try {

            if (externalBankRepository.count() <= 0) {

                ResponseEntity<?> rs = restTemplate.getForEntity(URL, BankApiDTO.class);

                if (rs.getStatusCode().equals(HttpStatus.OK)) {

                    BankApiDTO dto = (BankApiDTO) Objects.requireNonNull(rs.getBody());

                    externalBankRepository.saveAll(dto.getData().stream()
                            .map(bankApiMapper::responseToEntity).toList());

                    log.info("Database initialized: Add {} banks", dto.getData().size());
                }
            }
        } catch (Exception ignore) {

        }
    }
}
