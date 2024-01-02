package cn.biq.mn.user.balanceflow;

import cn.biq.mn.base.validation.ValidFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import cn.biq.mn.base.response.DataResponse;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/balance-flows")
@RequiredArgsConstructor
public class BalanceFlowController {

    private final BalanceFlowService balanceFlowService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody BalanceFlowAddForm form) {
        return new BaseResponse(balanceFlowService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(BalanceFlowQueryForm form, @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable page) {
        return new PageResponse<>(balanceFlowService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public BaseResponse handleGet(@PathVariable("id") Integer id) {
        return new DataResponse<>(balanceFlowService.get(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody BalanceFlowUpdateForm form) {
        return new BaseResponse(balanceFlowService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(balanceFlowService.remove(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/statistics")
    public BaseResponse handleStatistics(BalanceFlowQueryForm form) {
        return new DataResponse<>(balanceFlowService.statistics(form));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/confirm")
    public BaseResponse handleConfirm(@PathVariable("id") Integer id) {
        return new BaseResponse(balanceFlowService.confirm(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/addFile")
    public BaseResponse handleAddFile(@PathVariable("id") Integer id, @Validated @ValidFile @RequestParam("file") MultipartFile file) {
        return new DataResponse<>(balanceFlowService.addFile(id, file));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/files")
    public BaseResponse handleFiles(@PathVariable("id") Integer id) {
        return new DataResponse<>(balanceFlowService.getFiles(id));
    }

}
