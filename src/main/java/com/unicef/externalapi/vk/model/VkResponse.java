package com.unicef.externalapi.vk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VkResponse {
    @JsonProperty("count")
    private Long count;
    @JsonProperty("items")
    private List<VkUser> items;
}
