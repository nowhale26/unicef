package com.unicef.service.player;

import com.unicef.entity.PlayerEntity;
import com.unicef.service.player.model.Player;
import com.unicef.service.player.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerEntity savePlayer(Player player){
        Optional<PlayerEntity> existingPlayer = playerRepository.findByVkId(player.getVkId());
        if (existingPlayer.isPresent()) {
            return existingPlayer.get();
        }

        PlayerEntity playerEntity = new PlayerEntity();
        playerEntity.setFirstName(player.getFirstName());
        playerEntity.setLastName(player.getLastName());
        playerEntity.setVkId(player.getVkId());
        playerEntity.setBirthYear(player.getBirthYear());
        return playerRepository.save(playerEntity);
    }
}
