package cn.biq.mn.group;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RemoveUserForm {

    @NotNull
    private Integer userId;

}
