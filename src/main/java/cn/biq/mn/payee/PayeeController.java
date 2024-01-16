package cn.biq.mn.payee;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payees")
@RequiredArgsConstructor
public class PayeeController extends BaseController {

    private final PayeeService payeeService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody PayeeAddForm form) {
        return new DataResponse<>(payeeService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(
            @Valid PayeeQueryForm form,
            @PageableDefault(sort = "sort", direction = Sort.Direction.ASC) Pageable page
    ) {
        return new PageResponse<>(payeeService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody PayeeUpdateForm form) {
        return new BaseResponse(payeeService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(@Valid PayeeQueryForm form) {
        return new DataResponse<>(payeeService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(payeeService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanExpense")
    public BaseResponse handleToggleCanExpense(@PathVariable("id") Integer id) {
        return new BaseResponse(payeeService.toggleCanExpense(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanIncome")
    public BaseResponse handleToggleCanIncome(@PathVariable("id") Integer id) {
        return new BaseResponse(payeeService.toggleCanIncome(id));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(payeeService.remove(id));
    }

}