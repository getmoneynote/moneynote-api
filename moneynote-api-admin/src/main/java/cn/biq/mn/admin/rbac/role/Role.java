package cn.biq.mn.admin.rbac.role;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_admin_rbac_role")
@Getter @Setter
public class Role extends BaseEntity {

    private String name;

}
