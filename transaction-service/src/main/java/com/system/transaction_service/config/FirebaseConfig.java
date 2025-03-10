package com.system.transaction_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${network.proxy.host}")
    private String host;

    @Value("${network.proxy.port}")
    private String port;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {

        // Proxy config
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource("config/upload-file.json").getInputStream());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setStorageBucket("upload-file-2ac29.appspot.com")
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
