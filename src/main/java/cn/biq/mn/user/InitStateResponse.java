package cn.biq.mn.user;

import cn.biq.mn.book.BookSessionVo;
import cn.biq.mn.group.GroupSessionVo;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InitStateResponse {

    private UserSessionVo user;
    private BookSessionVo book;
    private GroupSessionVo group;

}
