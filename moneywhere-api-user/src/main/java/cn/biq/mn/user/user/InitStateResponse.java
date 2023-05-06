package cn.biq.mn.user.user;

import cn.biq.mn.user.book.BookSessionVo;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.user.group.GroupSessionVo;

@Getter @Setter
public class InitStateResponse {

    private UserSessionVo user;
    private BookSessionVo book;
    private GroupSessionVo group;

}
