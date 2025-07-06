package com.unicef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vk_user_group")
public class VkUserGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vk_group_id", nullable = false)
    private String vkGroupId;

    @Column(name = "is_player")
    private Boolean isPlayer;

    @Column(name = "vk_user_id")
    private Long vkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "vk_id")
    private PlayerEntity player;

}
