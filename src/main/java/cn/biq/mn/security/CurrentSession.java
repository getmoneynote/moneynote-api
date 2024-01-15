package cn.biq.mn.security;

import cn.biq.mn.book.Book;
import cn.biq.mn.group.Group;
import cn.biq.mn.user.User;
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
    private User user;
    private Book book;
    private Group group;

}
