package cn.biq.mn.user.user;

import cn.biq.mn.user.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.UserNameField;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "t_user_user")
@Getter @Setter
public class User extends BaseEntity {
    
    @Column(length = 16, unique = true, nullable = true)
    @UserNameField
    private String username;
    
    @Column(length = 16)
    @NameField
    private String nickName;
    
    @Column(length = 64)
    private String password;

    @Column(length = 16)
    @NameField
    private String telephone;
    
    @Column(length = 32)
    @Email
    private String email;
    
    @Column(length = 128)
    private String registerIp;

    @ManyToOne(fetch = FetchType.LAZY)
    private Group defaultGroup; //用户默认操作的组
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Book defaultBook; //用户默认操作的账本

    @Column(nullable = false)
    @NotNull
    private Boolean enable = true;

    @TimeField
    private Long registerTime; //注册时间

    @Column(length = 32)
    private String openId;

    @Column(length = 32, unique = true)
    private String unionId;

    @Column(length = 256)
    private String headimgurl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserGroupRelation> relations = new HashSet<>();

}
