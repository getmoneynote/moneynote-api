package cn.biq.mn.noteday;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/note-days")
@RequiredArgsConstructor
public class NoteDayController extends BaseController {

    private final NoteDayService noteDayService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody NoteDayAddForm form) {
        return new BaseResponse(noteDayService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(NoteDayQueryForm form, Pageable page) {
        return new PageResponse<>(noteDayService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody NoteDayUpdateForm form) {
        return new BaseResponse(noteDayService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(noteDayService.remove(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/run")
    public BaseResponse handleRun(@PathVariable("id") Integer id) {
        return new BaseResponse(noteDayService.run(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/recall")
    public BaseResponse handleRecall(@PathVariable("id") Integer id) {
        return new BaseResponse(noteDayService.recall(id));
    }

}