package cn.biq.mn.admin.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class UserQueryForm {

    private Boolean enable;
    private String username;
    private String nickName;
    private String telephone;
    private String email;

    public Predicate buildPredicate() {
        QUser user = QUser.user;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
//        if (enable != null) {
//            booleanBuilder.and(user.);
//        }
        return booleanBuilder;
    }

}
