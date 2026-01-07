package com.unicef.externalapi.hse;

import com.unicef.externalapi.hse.model.HseDumpEntity;
import com.unicef.externalapi.hse.model.HseTokenResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HseService {

    private final HseClient hseClient;
    private volatile String accessToken;
    private volatile long accessTokenExpiresAt;

    public HseService(HseClient hseClient) {
        this.hseClient = hseClient;
    }

    public boolean authorizeByCode(String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            return false;
        }

        HseTokenResponse token = hseClient.requestTokenByCode(authorizationCode);
        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            return false;
        }

        this.accessToken = token.getAccessToken();
        this.accessTokenExpiresAt = resolveExpiresAt(token.getAccessExpiresIn());
        return true;
    }

    public HseDumpEntity findPersonWithStoredToken(String fullName) {
        if (!isTokenValid()) {
            return null;
        }

        List<HseDumpEntity> results = hseClient.searchByNameWithToken(fullName, accessToken);
        return findByName(fullName, results);
    }

    public HseDumpEntity findPerson(String fullName) {
        List<HseDumpEntity> results = hseClient.searchByName(fullName);
        return findByName(fullName, results);
    }

    private HseDumpEntity findByName(String fullName, List<HseDumpEntity> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }

        for (HseDumpEntity entity : results) {
            String candidate = resolveName(entity);
            if (candidate != null && candidate.equalsIgnoreCase(fullName)) {
                return entity;
            }
        }

        return null;
    }

    private String resolveName(HseDumpEntity entity) {
        if (entity.getFullName() != null && !entity.getFullName().isBlank()) {
            return entity.getFullName();
        }
        if (entity.getLabel() != null && !entity.getLabel().isBlank()) {
            return entity.getLabel();
        }
        if (entity.getRoom() != null && !entity.getRoom().isBlank()) {
            return entity.getRoom();
        }
        return null;
    }

    private boolean isTokenValid() {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        if (accessTokenExpiresAt <= 0) {
            return true;
        }
        return System.currentTimeMillis() < accessTokenExpiresAt;
    }

    private long resolveExpiresAt(Long expiresIn) {
        if (expiresIn == null || expiresIn <= 0) {
            return 0L;
        }
        long millis = expiresIn;
        if (expiresIn < 100000000L) {
            millis = expiresIn * 1000L;
        }
        return System.currentTimeMillis() + millis;
    }
}
