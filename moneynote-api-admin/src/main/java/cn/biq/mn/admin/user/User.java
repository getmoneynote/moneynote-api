package cn.biq.mn.admin.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_user_user")
@Getter
@Setter
public class User extends BaseEntity {

    private String username;

    private String nickName;

    private String telephone;

    private String email;

    private String registerIp;

    private Boolean enable;

    private Long registerTime;

    private String unionId;

    private String headimgurl;

}
