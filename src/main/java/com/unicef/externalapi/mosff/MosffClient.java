package com.unicef.externalapi.mosff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
@Component
public class MosffClient {

    private final RestTemplate restTemplate;

    public MosffClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Document requestPlayers(String surnameName) {
        String url = "https://mosff.ru/search/players?q=" + surnameName.replace(" ", "+");
        try {
            String html = restTemplate.getForObject(url, String.class);
            if (html != null) {
                return Jsoup.parse(html);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе к mosff: " + e.getMessage());
            return null;
        }
    }
}
