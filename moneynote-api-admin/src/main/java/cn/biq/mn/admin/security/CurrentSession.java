package cn.biq.mn.admin.security;

import cn.biq.mn.admin.rbac.admin.Admin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
@Getter
@Setter
public class CurrentSession {

    private String accessToken;
    private Admin admin;

}
