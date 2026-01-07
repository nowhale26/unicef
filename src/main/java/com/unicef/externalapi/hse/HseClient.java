package com.unicef.externalapi.hse;

import com.unicef.ApplicationConfig;
import com.unicef.externalapi.hse.model.HseDumpEntity;
import com.unicef.externalapi.hse.model.HseTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class HseClient {

    private static final String TOKEN_URL = "https://saml.hse.ru/realms/hse/protocol/openid-connect/token";
    private static final String SEARCH_URL = "https://api.hseapp.ru/v3/dump/search";

    private final RestTemplate restTemplate;
    private final ApplicationConfig config;

    public HseClient(RestTemplate restTemplate, ApplicationConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public List<HseDumpEntity> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String accessToken = requestAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        return requestSearch(name, accessToken);
    }

    public List<HseDumpEntity> searchByNameWithToken(String name, String accessToken) {
        if (name == null || name.isBlank() || accessToken == null || accessToken.isBlank()) {
            return null;
        }

        return requestSearch(name, accessToken);
    }

    public HseTokenResponse requestTokenByCode(String authorizationCode) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", authorizationCode);
            body.add("client_id", config.hseClientId());
            body.add("redirect_uri", config.hseRedirectUri());
            body.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<HseTokenResponse> response = restTemplate.postForEntity(
                    TOKEN_URL,
                    request,
                    HseTokenResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to request HSE access token by code: {}", e.getMessage());
            return null;
        }
    }

    private String requestAccessToken() {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", config.hseClientId());
            body.add("username", config.hseLogin());
            body.add("password", config.hsePassword());
            body.add("grant_type", "password");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<HseTokenResponse> response = restTemplate.postForEntity(
                    TOKEN_URL,
                    request,
                    HseTokenResponse.class
            );

            HseTokenResponse token = response.getBody();
            return token != null ? token.getAccessToken() : null;
        } catch (Exception e) {
            log.error("Failed to request HSE access token: {}", e.getMessage());
            return null;
        }
    }

    private List<HseDumpEntity> requestSearch(String name, String accessToken) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                    .queryParam("q", name)
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<List<HseDumpEntity>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to request HSE search results: {}", e.getMessage());
            return null;
        }
    }
}
