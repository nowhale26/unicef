package com.unicef.common.validator;

import com.unicef.externalapi.mosff.model.MosffPlayer;
import com.unicef.externalapi.vk.model.University;
import com.unicef.externalapi.vk.model.VkUser;

import java.util.List;

public final class BachelorPlayerValidator implements CorrectPlayerValidator {
    public static final BachelorPlayerValidator INSTANCE = new BachelorPlayerValidator();

    private BachelorPlayerValidator() {
    }

    ;

    @Override
    public boolean checkPlayer(VkUser vkUser, MosffPlayer mosffPlayer) {
        String bdateVk = vkUser.getBdate();
        List<University> universities = vkUser.getUniversities();
        if (bdateVk != null) {
            String[] bdateVKParts = bdateVk.split("\\.");
            String[] mosffPlayerBdateParts = mosffPlayer.getDateOfBirth().split("\\.");
            if (!(bdateVKParts[0].equals(mosffPlayerBdateParts[0]) && bdateVKParts[1].equals(mosffPlayerBdateParts[1]))) {
                return false;
            }
        }
        if (universities != null && !universities.isEmpty()) {
            if (universities.size() == 1) {
                String university = universities.get(0).getName();
                if (!university.equals("НИУ ВШЭ (ГУ-ВШЭ)")) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

}
