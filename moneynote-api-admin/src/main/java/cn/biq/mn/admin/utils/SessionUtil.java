package cn.biq.mn.admin.utils;

import cn.biq.mn.admin.rbac.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cn.biq.mn.admin.security.CurrentSession;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final CurrentSession session;

    public Admin getCurrentAdmin() {
        return session.getAdmin();
    }

    public void clear() {
        session.setAccessToken(null);
        session.setAdmin(null);
    }

}
