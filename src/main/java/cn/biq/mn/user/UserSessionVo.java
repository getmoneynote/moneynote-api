package cn.biq.mn.user;

import cn.biq.mn.base.BaseDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionVo extends BaseDetails {

    public String username;

    @JsonProperty("name")
    public String nickName;

    public String headimgurl;

}
