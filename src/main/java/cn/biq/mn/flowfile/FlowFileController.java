package cn.biq.mn.flowfile;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/flow-files")
@RequiredArgsConstructor
public class FlowFileController extends BaseController {

    private final FlowFileService flowFileService;

    @RequestMapping(method = RequestMethod.GET, value = "/view")
    public ResponseEntity<byte[]> handleView(@Valid FlowFileViewForm form) {
        FlowFile flowFile = flowFileService.getFile(form);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(flowFile.getContentType())).body(flowFile.getData());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(flowFileService.remove(id));
    }

}