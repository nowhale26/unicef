package com.unicef.externalapi.mosff;

import com.unicef.ApplicationConfig;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MosffService {

    private final MosffClient mosffClient;
    private final ApplicationConfig config;

    public MosffService(MosffClient mosffClient, ApplicationConfig config) {
        this.mosffClient = mosffClient;
        this.config = config;
    }

    public MosffPlayer findPlayer(String surnameName) {
        Document doc = mosffClient.requestPlayers(surnameName);
        if (doc != null) {
            Elements playerItems = doc.select("ul.composition__list li.composition__item");
            List<MosffPlayer> players = new ArrayList<>();
            for (Element item : playerItems) {
                MosffPlayer player = new MosffPlayer();

                Element nameElement = item.selectFirst("div.composition__title");
                player.setName(nameElement != null ? nameElement.text() : "");


                Element teamElement = item.selectFirst("div.composition__teams");
                player.setTeam(teamElement != null ? teamElement.text() : "");

                Elements infoElements = item.select("div.composition__info div");
                if (!infoElements.isEmpty()) {
                    player.setPosition(infoElements.get(0).text());
                }
                if (infoElements.size() > 1) {
                    player.setDateOfBirth(infoElements.get(1).text());
                }

                players.add(player);
            }

            int minYear = Integer.parseInt(config.minYear());
            int maxYear = Integer.parseInt(config.maxYear());

            for (var player : players) {
                String[] nameParts = player.getName().split(" ");
                String playerNameSurname = nameParts[0] + " " + nameParts[1];
                int birthYear = Integer.parseInt(player.getDateOfBirth().split("\\.")[2]);
                if (surnameName.equalsIgnoreCase(playerNameSurname) && minYear<= birthYear && birthYear<=maxYear){
                    player.setDateOfBirth(Integer.toString(birthYear));
                    return player;
                }
            }
            return null;
        }
        return null;
    }
}
