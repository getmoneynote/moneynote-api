package cn.biq.mn.admin.rbac.admin;

import cn.biq.mn.admin.rbac.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_admin_rbac_user")
@Getter @Setter
public class Admin extends BaseEntity {

    @Column(length = 16, unique = true, nullable = false)
    private String username;

    @Column(length = 64, nullable = false)
    private String password;

    @ManyToMany
    private Set<Role> roles = new HashSet<>();

}
