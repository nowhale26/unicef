package com.unicef.externalapi.hse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HseDumpEntity {
    private String id;
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    private String label;
    private String room;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String description;
    private String type;
}
