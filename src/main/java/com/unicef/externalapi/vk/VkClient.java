package com.unicef.externalapi.vk;

import com.unicef.ApplicationConfig;
import com.unicef.common.ErrorApplier;
import com.unicef.externalapi.vk.model.VkAnswer;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class VkClient {

    private final WebClient vkWebClient;

    private final String vkToken;


    public VkClient(WebClient vkWebClient, ApplicationConfig config) {
        this.vkWebClient = vkWebClient;
        this.vkToken = config.vkToken();
    }

    public VkAnswer getVkMembers(int offset, String groupId, String methodUrl){
        String entityIdName;
        if(methodUrl.equals("/friends.get")){
            entityIdName = "user_id";
        } else{
            entityIdName = "group_id";
        }
        String finalEntityIdName = entityIdName;
        return vkWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(methodUrl)
                        .queryParam("access_token", vkToken)
                        .queryParam(finalEntityIdName,groupId)
                        .queryParam("offset",offset)
                        .queryParam("fields","bdate,sex,universities")
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
