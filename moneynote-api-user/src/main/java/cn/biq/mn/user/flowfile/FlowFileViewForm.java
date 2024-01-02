package cn.biq.mn.user.flowfile;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


@Getter @Setter
public class FlowFileViewForm {

    @NonNull
    private Integer id;

    // 时间是为了增加地址的安全性
    @NonNull
    private Long createTime;

}
