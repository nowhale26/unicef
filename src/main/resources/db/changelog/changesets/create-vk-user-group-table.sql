CREATE TABLE IF NOT EXISTS vk_user_group (
  id BIGSERIAL PRIMARY KEY,
  vk_group_id TEXT NOT NULL,
  is_player BOOLEAN,
  vk_user_id BIGINT,
  player_id BIGINT,
  CONSTRAINT fk_player_id FOREIGN KEY (player_id) REFERENCES player(vk_id) ON DELETE CASCADE
);