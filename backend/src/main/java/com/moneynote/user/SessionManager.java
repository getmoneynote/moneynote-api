package com.moneynote.user;

import com.moneynote.utils.CacheHolder;

import java.util.UUID;

public class SessionManager {

    public static String createSession(User user) {
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setCurrentUser(user);
        CacheHolder.sessionInfo.put(user.getId(), sessionInfo);

        String token = UUID.randomUUID().toString().replace("-", "");
        CacheHolder.token.put(token, user.getId()); // 最后一步才暴露token，保证能查到token时数据已经完整

        return token;
    }

    public static void invalidateSession(String token) {
        Long userId = CacheHolder.token.remove(token);
        if (userId == null) {
            return;
        }
        // sessionInfo 以 userId 为键，同一用户多端登录时共用一条会话。
        // 直接删会话会让其他端的 token 仍通过拦截器、却在 getCurrentUser 处 NPE，
        // 因此仅当该用户已无其他 token 时才删除会话。
        if (!CacheHolder.token.containsValue(userId)) {
            CacheHolder.sessionInfo.remove(userId);
        }
    }

    public static SessionInfo getSessionInfo(Long userId) {
        return CacheHolder.sessionInfo.get(userId);
    }

    public static User getCurrentUser() {
        return CacheHolder.sessionInfo.get(CurrentUserHolder.get()).getCurrentUser();
    }

}
