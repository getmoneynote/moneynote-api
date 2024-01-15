package cn.biq.mn.flowfile;

import cn.biq.mn.base.BaseDetails;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowFileDetails extends BaseDetails {

    private Long createTime;
    private String contentType;
    private Long size;
    private String originalName;

    public boolean isImage() {
        return contentType.equals("image/jpeg") || contentType.equals("image/png");
    }

}
