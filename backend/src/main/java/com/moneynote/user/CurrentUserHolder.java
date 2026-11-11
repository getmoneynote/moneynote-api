package com.moneynote.user;

public class CurrentUserHolder {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    public static Long get() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }
}