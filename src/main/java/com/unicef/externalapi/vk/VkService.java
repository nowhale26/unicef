package com.unicef.externalapi.vk;

import com.unicef.externalapi.vk.model.VkAnswer;
import com.unicef.externalapi.vk.model.VkResponse;
import com.unicef.externalapi.vk.model.VkUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VkService {

    private final VkClient client;

    public VkService(VkClient client) {
        this.client = client;
    }

    public List<VkUser> getGroupMembers(int offset, String groupId){
        VkAnswer response = client.getMembers(offset,groupId);
        return response.getResponse().getItems();
    }
}
