package cn.biq.mn.user.group;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InviteUserForm {

    @NotBlank
    private String username;

}
