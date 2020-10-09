package org.eja.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Service
public class SmsService {
    private final static String SMS_PROVIDER_API_ENDPOINT = "https://api.sms.to/sms/send";
    private final static String SMS_PROVIDER_API_KEY = "3sfKv85So78p3Ax7Lm570RlcEi6X621I";
    private final static String SMS_SENDER_ID = "FS React";

    private final ObjectMapper objectMapper;

    public SmsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendSmsPinCode(String to, String smsPinCode) throws Exception {
        log.info("Sending SMS PIN Code(to={}, senderId={}, smsPinCode={})", to, SMS_SENDER_ID, smsPinCode);

        String smsRequestBodyString = objectMapper.writeValueAsString(Map.of(
                "to", to,
                "sender_id", SMS_SENDER_ID,
                "message", "Your FS SMS Pin Code is " + smsPinCode
        ));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(smsRequestBodyString);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SMS_PROVIDER_API_ENDPOINT))
                .headers(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer " + SMS_PROVIDER_API_KEY
                )
                .POST(bodyPublisher)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpStatus.OK.value()) {
            String errorMessage = String.format("Error sending sms: %s", response.body());
            throw new Exception(errorMessage);
        }

        log.info("SMS PIN Code sent(to={}, senderId={}, smsPinCode={})", to, SMS_SENDER_ID, smsPinCode);
    }
}
