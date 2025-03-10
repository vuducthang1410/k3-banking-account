package org.demo.loanservice.wiremockService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.loanservice.dto.CICRequest;
import org.demo.loanservice.dto.CICResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CICService {

    private final WireMockServer wireMockServer;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void startWireMock() {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            System.out.println("WireMock server started on port " + wireMockServer.port());
        }
        setupStub();
    }

    private void setupStub() {
        Random random = new Random();
        int randomCreditScore = random.nextInt(551) + 300;
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/credit-score/cic"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                                    {
                                        "cccd": "079123456789",
                                        "credit_score": %d,
                                        "credit_rating": "%s",
                                        "debt_status": "No bad debt",
                                        "last_updated": "2025-02-04T10:30:00Z"
                                    }
                                """, randomCreditScore, getCreditRating(randomCreditScore))
                        )
                )
        );
    }

    public CICResponse getCreditScore(String cccd, String fullName, String dob, String phoneNumber) {
        String url = "http://localhost:8386/api/credit-score/cic";
        Random random = new Random();
        int randomCreditScore = random.nextInt(551) + 300;
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(new CICRequest(cccd, fullName, dob, phoneNumber)),
                    String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            CICResponse cicResponse = objectMapper.readValue(responseEntity.getBody(), CICResponse.class);
            cicResponse.setCreditScore(randomCreditScore);
            cicResponse.setCreditRating(getCreditRating(randomCreditScore));
            cicResponse.setCccd(cccd);
            return cicResponse;

        } catch (Exception e) {
           log.error(e.getMessage());
        }
        return null;
    }

    private String getCreditRating(int creditScore) {
        if (creditScore >= 750) {
            return "Excellent";
        } else if (creditScore >= 700) {
            return "Good";
        } else if (creditScore >= 650) {
            return "Fair";
        } else if (creditScore >= 600) {
            return "Average";
        } else {
            return "Poor";
        }
    }
}