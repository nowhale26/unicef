package com.unicef.service.player.model;

import lombok.Data;

@Data
public class Player {
    private Long vkId;
    private String firstName;
    private String lastName;
    private Integer birthYear;
}
