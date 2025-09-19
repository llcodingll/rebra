package com.rebra.config;

import java.security.Principal;

/**
 * WebSocket 세션에서 사용자 인증을 위한 Principal 구현체
 */
public class UserPrincipal implements Principal {

    private final String userId;

    public UserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }

    public Long getUserId() {
        return Long.parseLong(userId);
    }

    @Override
    public String toString() {
        return "UserPrincipal{userId=" + userId + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserPrincipal that = (UserPrincipal) obj;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}