package com.unicef.externalapi.ufl;

import com.unicef.ApplicationConfig;
import com.unicef.externalapi.hse.HseService;
import com.unicef.externalapi.mosff.model.MosffPlayer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class UflService {

    private final UflClient uflClient;
    private final HseService hseService;
    private final ApplicationConfig config;
    private final String season24 = "10123706";
    private final String season23 = "10118997";

    public UflService(UflClient uflClient, HseService hseService, ApplicationConfig config) {
        this.uflClient = uflClient;
        this.hseService = hseService;
        this.config = config;
    }

    public MosffPlayer findPlayer(String surnameName){
        Document doc = uflClient.searchPlayers(surnameName, season24);

        if(doc!=null){
            var player = documentSearch(doc, surnameName);
            if(player!=null){
                return player;
            }
        } else{
            doc = uflClient.searchPlayers(surnameName, season23);
            return documentSearch(doc, surnameName);
        }
        return null;
    }

    public void getAndCheckAllUflPlayers(Long tgChatId, int startPage){
        List<MosffPlayer> allPlayers = new ArrayList<>();

        // Загружаем первую страницу для определения общего количества страниц
        Document firstPage = uflClient.searchAllPlayers(1);
        if (firstPage == null) {
            return;
        }

        int totalPages = getTotalPages(firstPage);
        log.info("ЮФЛ: Всего страниц для парсинга: {}. Начинаем с страницы: {}", totalPages, startPage);

        // Обрабатываем со стартовой страницы
        for (int page = startPage; page <= totalPages; page++) {
            try{
                Thread.sleep(500);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                log.error("Ошибка при добавлении задержки", e);
            }
            Document doc = uflClient.searchAllPlayers(page);
            if (doc != null) {
                hseService.checkUflPlayersInAppX(tgChatId, parsePlayersFromPage(doc));
            }
            log.info("ЮФЛ: Обработано страниц: {}/{}", page, totalPages);
        }

        log.info("ЮФЛ: Все страницы обработаны");
    }

    private int getTotalPages(Document doc) {
        Element lastPageLink = doc.selectFirst("ul.pagination-section li:last-child a[data-page]");
        if (lastPageLink != null) {
            String dataPage = lastPageLink.attr("data-page");
            try {
                return Integer.parseInt(dataPage) + 1;
            } catch (NumberFormatException e) {
                log.error("Ошибка парсинга номера страницы: {}", dataPage);
            }
        }
        return 1;
    }

    private List<MosffPlayer> parsePlayersFromPage(Document doc) {
        List<MosffPlayer> players = new ArrayList<>();
        Elements playerLinks = doc.select("a.table__player[href^='/player/']");

        for (Element playerLinkElement : playerLinks) {
            try{
                Thread.sleep(500);
                Document profileDoc = uflClient.requestPlayerProfile(playerLinkElement);

                if (profileDoc != null) {
                    MosffPlayer player = new MosffPlayer();

                    String firstName = profileDoc.selectFirst("span.player-promo__name-main") != null
                            ? profileDoc.selectFirst("span.player-promo__name-main").text()
                            : "";
                    String middleName = profileDoc.selectFirst("span.player-promo__name-middle") != null
                            ? profileDoc.selectFirst("span.player-promo__name-middle").text()
                            : "";

                    String fullName = middleName.isEmpty() ? firstName : firstName + " " + middleName;
                    player.setName(fullName);

                    String club = profileDoc.selectFirst("li.player-promo__item--club .player-promo__value") != null
                            ? profileDoc.selectFirst("li.player-promo__item--club .player-promo__value").text()
                            : "";
                    player.setTeam(club);

                    String position = profileDoc.selectFirst("li.player-promo__item--role .player-promo__value") != null
                            ? profileDoc.selectFirst("li.player-promo__item--role .player-promo__value").text()
                            : "";
                    player.setPosition(position);

                    String birthDate = profileDoc.selectFirst("li.player-promo__item--birth .player-promo__value") != null
                            ? profileDoc.selectFirst("li.player-promo__item--birth .player-promo__value").text()
                            : "";
                    if (!birthDate.isEmpty()) {
                        birthDate = convertDateFormat(birthDate);
                    }
                    player.setDateOfBirth(birthDate);

                    players.add(player);
                    log.info("Обработан игрок: {}", player);
                }
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                log.error("Ошибка при добавлении задержки", e);
            } catch (Exception e) {
                log.error("Ошибка во время поиска игрока: ",e);
            }

        }
        return players;
    }

    public MosffPlayer documentSearch(Document doc, String surnameName){
        Elements playerItems = doc.select("a.table__player[href^='/player/']");
        if (playerItems.isEmpty()) {
            return null;
        }

        List<MosffPlayer> players = new ArrayList<>();
        for (Element playerLinkElement : playerItems) {
            Document profileDoc = uflClient.requestPlayerProfile(playerLinkElement);

            if(profileDoc!=null){
                MosffPlayer player = new MosffPlayer();

                String fullName = profileDoc.selectFirst("span.player-promo__name-main") != null
                        ? profileDoc.selectFirst("span.player-promo__name-main").text()
                        : "";
                player.setName(fullName);

                String club = profileDoc.selectFirst("li.player-promo__item--club .player-promo__value") != null
                        ? profileDoc.selectFirst("li.player-promo__item--club .player-promo__value").text()
                        : "";
                player.setTeam(club);

                String birthDate = profileDoc.selectFirst("li.player-promo__item--birth .player-promo__value") != null
                        ? profileDoc.selectFirst("li.player-promo__item--birth .player-promo__value").text()
                        : "";

                if(!birthDate.isEmpty()){
                    birthDate = convertDateFormat(birthDate);
                } else{
                    log.error("Юфл: Нет дня рождения: "+surnameName);
                }
                player.setDateOfBirth(birthDate);
                players.add(player);
            }
        }

        int minYear = Integer.parseInt(config.minYear());
        int maxYear = Integer.parseInt(config.maxYear());
        String normalizedSurnameName = normalizeName(surnameName);

        for (var player : players) {
            String[] nameParts = player.getName().split(" ");
            String playerSurnameName = nameParts[0] + " " + nameParts[1];
            String normalizedPlayerSurnameName = normalizeName(playerSurnameName);
            int birthYear = Integer.parseInt(player.getDateOfBirth().split("\\.")[2]);
            if (normalizedSurnameName.equalsIgnoreCase(normalizedPlayerSurnameName) && minYear<= birthYear && birthYear<=maxYear){
                return player;
            }
        }
        return null;
    }

    public static String convertDateFormat(String inputDate) {
        Map<String, String> MONTH_MAP = new HashMap<>();
        MONTH_MAP.put("января", "1");
        MONTH_MAP.put("февраля", "2");
        MONTH_MAP.put("марта", "3");
        MONTH_MAP.put("апреля", "4");
        MONTH_MAP.put("мая", "5");
        MONTH_MAP.put("июня", "6");
        MONTH_MAP.put("июля", "7");
        MONTH_MAP.put("августа", "8");
        MONTH_MAP.put("сентября", "9");
        MONTH_MAP.put("октября", "10");
        MONTH_MAP.put("ноября", "11");
        MONTH_MAP.put("декабря", "12");

        String[] parts = inputDate.trim().split("\\s+");

        if(parts.length>0){
            String day = parts[0];
            if(day.charAt(0)=='0'){
                day = day.substring(1);
            }
            String month = parts[1].toLowerCase();
            String year = parts[2];

            String monthNumber = MONTH_MAP.get(month);
            return String.format("%s.%s.%s", day, monthNumber, year);
        } else{
            log.error("Юфл Ошибка с датой рождения: "+ inputDate);
            return "";
        }

    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.replace('ё', 'е').replace('Ё', 'Е');
    }
}
