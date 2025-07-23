package com.unicef.common.validator;

import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.vk.model.VkUser;

public interface CorrectPlayerValidator {
    boolean checkPlayer(VkUser vkUser, MosffPlayer mosffPlayer);
}
