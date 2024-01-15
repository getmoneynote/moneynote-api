package cn.biq.mn.group;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InviteUserForm {

    @NotBlank
    private String username;

}
