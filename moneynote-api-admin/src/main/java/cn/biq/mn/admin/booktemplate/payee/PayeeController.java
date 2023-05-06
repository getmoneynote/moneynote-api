package cn.biq.mn.admin.booktemplate.payee;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;


@RestController
@RequestMapping("/payees")
@RequiredArgsConstructor
public class PayeeController {

    private final PayeeService payeeService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(Pageable page, PayeeQueryForm form) {
        return new PageResponse<>(payeeService.query(page, form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody PayeeAddForm form) {
        return new BaseResponse(payeeService.add(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id,  @Valid @RequestBody PayeeUpdateForm form) {
        return new BaseResponse(payeeService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(payeeService.remove(id));
    }

}
