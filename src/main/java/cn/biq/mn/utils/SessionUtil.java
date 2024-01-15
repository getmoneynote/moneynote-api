package cn.biq.mn.utils;

import cn.biq.mn.book.Book;
import cn.biq.mn.group.Group;
import cn.biq.mn.security.CurrentSession;
import cn.biq.mn.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final CurrentSession session;

    public User getCurrentUser() {
        return session.getUser();
    }

    public void clear() {
        session.setAccessToken(null);
        session.setUser(null);
        session.setBook(null);
        session.setGroup(null);
    }

    public Book getCurrentBook() {
        return session.getBook();
    }

    public Group getCurrentGroup() {
        return session.getGroup();
    }

    public void setCurrentBook(Book book) {
        session.setBook(book);
    }

    public void setCurrentGroup(Group group) {
        session.setGroup(group);
    }


}
