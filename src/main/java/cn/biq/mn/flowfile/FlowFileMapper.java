package cn.biq.mn.flowfile;


public class FlowFileMapper {

    public static FlowFileDetails toDetails(FlowFile entity) {
        if (entity == null) return null;
        var details = new FlowFileDetails();
        details.setId(entity.getId());
        details.setCreateTime(entity.getCreateTime());
        details.setContentType(entity.getContentType());
        details.setSize(entity.getSize());
        details.setOriginalName(entity.getOriginalName());
        return details;
    }

}
