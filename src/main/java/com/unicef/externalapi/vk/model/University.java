package com.unicef.externalapi.vk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class University {
    @JsonProperty("name")
    private String name;
}
