package com.unicef.externalapi.vk;

import com.unicef.ApplicationConfig;
import com.unicef.common.ErrorApplier;
import com.unicef.externalapi.vk.model.VkAnswer;
import com.unicef.externalapi.vk.model.VkResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class VkClient {

    private final WebClient vkWebClient;

    private final String vkToken;
    private static final String getMembersUrl = "/groups.getMembers";

    public VkClient(WebClient vkWebClient, ApplicationConfig config) {
        this.vkWebClient = vkWebClient;
        this.vkToken = config.vkToken();
    }

    public VkAnswer getMembers(int offset, String groupId){
        return vkWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(getMembersUrl)
                        .queryParam("access_token", vkToken)
                        .queryParam("group_id",groupId)
                        .queryParam("offset",offset)
                        .queryParam("fields","sex")
                        .queryParam("lang",0)
                        .queryParam("v","5.199")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ErrorApplier::applyError)
                .onStatus(HttpStatusCode::isError, ErrorApplier::applyError)
                .bodyToMono(VkAnswer.class)
                .block();
    }
}
