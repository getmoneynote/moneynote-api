package cn.biq.mn.tagrelation;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag-relations")
@RequiredArgsConstructor
public class TagRelationController extends BaseController {

    private final TagRelationService tagRelationService;

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody TagRelationUpdateForm form) {
        return new BaseResponse(tagRelationService.update(id, form));
    }

}
