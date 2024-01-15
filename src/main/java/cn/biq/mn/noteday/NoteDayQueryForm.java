package cn.biq.mn.noteday;

import cn.biq.mn.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NoteDayQueryForm {

    private String title;

    public Predicate buildPredicate(User user) {
        QNoteDay noteDay = QNoteDay.noteDay;
        BooleanBuilder expression = new BooleanBuilder(noteDay.user.eq(user));
        if (title != null) {
            expression.and(noteDay.title.contains(title));
        }
        return expression;
    }

}
