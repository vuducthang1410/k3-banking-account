package org.demo.loanservice.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WireMockConfig {
    @Bean
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8386));
        wireMockServer.start();
        return wireMockServer;
    }
}
