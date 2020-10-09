package org.eja.api;

import lombok.extern.slf4j.Slf4j;
import org.eja.api.model.AuthenticationRequest;
import org.eja.data.repository.UserDirectoryRepository;
import org.eja.props.FreeSwitchProps;
import org.eja.service.SmsService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/user-directory")
public class UserDirectoryController {
    private final RestTemplate restTemplate;
    private final FreeSwitchProps freeSwitchProps;
    private final UserDirectoryRepository userDirectoryRepository;
    private final SmsService smsService;

    public UserDirectoryController(RestTemplateBuilder restTemplateBuilder,
                                   FreeSwitchProps freeSwitchProps,
                                   UserDirectoryRepository userDirectoryRepository,
                                   SmsService smsService) {
        this.restTemplate = restTemplateBuilder.build();
        this.freeSwitchProps = freeSwitchProps;
        this.userDirectoryRepository = userDirectoryRepository;
        this.smsService = smsService;
    }

    @PostMapping
    public Object authenticate(@RequestBody AuthenticationRequest authRequest)
            throws Exception {
        if (!this.userExists(authRequest.getUserId(), authRequest.getDomain())) {
            userDirectoryRepository.save(authRequest.getUserId(),
                    authRequest.getPassword(),
                    authRequest.getDisplayName());
        }

        smsService.sendSmsPinCode(authRequest.getUserId(), authRequest.getSmsPinCode());

        var response = reloadDirectory();
        if (response.getStatusCode() != HttpStatus.OK && !Objects.equals(response.getBody(), "+OK [Success]\n")) {
            throw new Exception("Couldn't reload xml.");
        }

        return authRequest;
    }

    @GetMapping
    public Object findAll() throws Exception {
        return userDirectoryRepository.getAllUsers();
    }

    private boolean userExists(String userId, String domain) {
        String url = String.format(freeSwitchProps.getWebapi() + "/user_exists?id %s %s", userId, domain);

        ResponseEntity<String> response = restTemplate.exchange(url,
                HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), String.class);

        return response.getStatusCode() == HttpStatus.OK && Objects.equals(response.getBody(), "true");
    }

    private ResponseEntity<String> reloadDirectory() throws Exception {
        return restTemplate.exchange(freeSwitchProps.getWebapi() + "/reloadxml",
                HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), String.class);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        String base64Credentials = Base64.getEncoder().encodeToString("freeswitch:works".getBytes());
        headers.add("Authorization", "Basic " + base64Credentials);

        return headers;
    }
}
