package com.unicef.externalapi.ufl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class UflClient {

    private final RestTemplate restTemplate;

    public UflClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Document searchPlayers(String surnameName, String seasonId) {
        surnameName = surnameName.replace(" ", "+");
        String searchUrl = String.format(
                "https://yflrussia.ru/participants/players?season_id=%s&name=%s",
                seasonId, surnameName
        );

        String searchHtml = restTemplate.getForObject(searchUrl, String.class);
        if (searchHtml == null) {
            return null;
        }

        return Jsoup.parse(searchHtml);

    }

    public Document requestPlayerProfile(Element playerLinkElement){
        String playerProfileUrl = "https://yflrussia.ru" + playerLinkElement.attr("href");

        String profileHtml = restTemplate.getForObject(playerProfileUrl, String.class);
        if (profileHtml == null) {
            return null;
        }

        return Jsoup.parse(profileHtml);
    }
}
