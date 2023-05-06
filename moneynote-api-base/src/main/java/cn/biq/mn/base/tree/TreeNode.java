package cn.biq.mn.base.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.IdAndNameDetails;

import java.util.List;

@Getter
@Setter
public class TreeNode<T> extends IdAndNameDetails {

    @JsonProperty("pId")
    private Integer parentId;
    private List<T> children;

}
