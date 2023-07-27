package cn.biq.mn.user.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.base.BaseController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import cn.biq.mn.base.response.DataResponse;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController extends BaseController {

    private final AccountService accountService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody AccountAddForm form) {
        return new BaseResponse(accountService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(
            AccountQueryForm form,
            @PageableDefault(sort = "balance", direction = Sort.Direction.DESC) Pageable page) {
        return new PageResponse<>(accountService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public BaseResponse handleGet(@PathVariable("id") Integer id) {
        return new DataResponse<>(accountService.get(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/statistics")
    public BaseResponse handleStatistics(AccountQueryForm form) {
        return new DataResponse<>(accountService.statistics(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody AccountUpdateForm form) {
        return new BaseResponse(accountService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleRemove(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.remove(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/delete")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.delete(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/recover")
    public BaseResponse handleRecover(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.recover(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(AccountQueryForm form) {
        return new DataResponse<>(accountService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleInclude")
    public BaseResponse handleToggleInclude(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggleInclude(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanExpense")
    public BaseResponse handleToggleCanExpense(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggleCanExpense(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanIncome")
    public BaseResponse handleToggleCanIncome(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggleCanIncome(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanTransferFrom")
    public BaseResponse handleToggleCanTransferFrom(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggleCanTransferFrom(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanTransferTo")
    public BaseResponse handleToggleCanTransferTo(@PathVariable("id") Integer id) {
        return new BaseResponse(accountService.toggleCanTransferTo(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/adjust")
    public BaseResponse handleAdjust(@PathVariable("id") Integer id, @Valid @RequestBody AdjustBalanceAddForm form) {
        return new BaseResponse(accountService.adjustBalance(id, form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/adjust")
    public BaseResponse handleUpdateAdjust(@PathVariable("id") Integer id, @Valid @RequestBody AdjustBalanceUpdateForm form) {
        return new BaseResponse(accountService.updateAdjustBalance(id, form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/overview")
    public BaseResponse handleOverview() {
        return new DataResponse<>(accountService.overview());
    }

}