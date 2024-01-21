package cn.biq.mn.tree;

import cn.biq.mn.base.IdAndNameDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TreeNode<T> extends IdAndNameDetails {

    @JsonProperty("pId")
    private Integer parentId;
    private List<T> children;

    private IdAndNameDetails parent;

}
