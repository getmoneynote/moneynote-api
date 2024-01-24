package cn.biq.mn.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectVo {

    private String value;
    private String label;

    public String getTitle() {
        return label;
    }

}
