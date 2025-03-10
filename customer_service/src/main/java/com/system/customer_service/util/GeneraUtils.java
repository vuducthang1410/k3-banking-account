package com.system.customer_service.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;

public class GeneraUtils {

    /**
     * Generates a unique ID by combining the current time
     * and a randomly generated Base64 encoded string.
     *
     * @return A unique ID string created from the current time and a random string.
     */
    public static String generateId(){
        // Step 1: Get the current time and format it as yyyyMMddHHmmss
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String timestamp = now.format(formatter);

        // Step 2: Create a random string and encode it in Base64
        byte[] randomBytes = new byte[6]; // Generate 6 random bytes
        new Random().nextBytes(randomBytes);
        String randomBase64 = Base64.getEncoder().encodeToString(randomBytes);

        // Step 3: Combine the timestamp and randomBase64 to create the ID
        return timestamp + randomBase64.replaceAll("[^a-zA-Z0-9]", ""); // Remove any '=' characters if present
    }
}
