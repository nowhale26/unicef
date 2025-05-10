package com.unicef.externalapi.vk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VkAnswer {
    @JsonProperty("response")
    private VkResponse response;
}
