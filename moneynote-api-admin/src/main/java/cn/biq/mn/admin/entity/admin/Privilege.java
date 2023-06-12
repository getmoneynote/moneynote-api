package cn.biq.mn.admin.entity.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_admin_rbac_privilege")
@Getter @Setter
public class Privilege extends BaseEntity {

    private String name;

}
