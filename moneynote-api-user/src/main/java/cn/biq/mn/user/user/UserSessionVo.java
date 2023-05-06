package cn.biq.mn.user.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;

@Getter
@Setter
public class UserSessionVo extends BaseDetails {

    public String username;

    @JsonProperty("name")
    public String nickName;

    public String headimgurl;

}
