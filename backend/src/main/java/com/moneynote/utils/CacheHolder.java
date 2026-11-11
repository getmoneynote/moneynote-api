package com.moneynote.utils;

import com.moneynote.user.SessionInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheHolder {

    public static final Map<String, Long> token = new ConcurrentHashMap<>();

    public static final Map<Long, SessionInfo> sessionInfo = new ConcurrentHashMap<>();


}
