package com.moneynote.account;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.moneynote.response.DataResponse;
import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * list 接口默认排序：displayOrder 升序、空值排最后，同序值内按余额降序（保持旧默认的余额观感），
     * 再以 id 兜底保证分页稳定。nullsLast 无法用 @PageableDefault 表达，故请求未带 sort 参数时
     * 在此代码级注入；带了 sort 参数（表头点击排序）则完全尊重请求。
     */
    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("displayOrder").nullsLast(),
            Sort.Order.desc("balance"),
            Sort.Order.asc("id"));

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody AccountCreateForm form) {
        accountService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody AccountUpdateForm form) {
        accountService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        accountService.delete(id);
        return new SimpleResponse();
    }

    @GetMapping
    public SimpleResponse list(AccountQueryForm form, @PageableDefault Pageable page) {
        Pageable effective = page.getSort().isUnsorted()
                ? PageRequest.of(page.getPageNumber(), page.getPageSize(), DEFAULT_SORT)
                : page;
        return new PageResponse<>(accountService.query(form, effective));
    }

    /** 账户下拉选项：轻量、不分页，type/canXxx 过滤语义见 AccountOptionQueryForm */
    @GetMapping("/options")
    public SimpleResponse options(AccountOptionQueryForm form) {
        return new DataResponse<>(accountService.listOptions(form));
    }

    @GetMapping("/balanceSummary")
    public SimpleResponse balanceSummary(AccountQueryForm form) {
        return new DataResponse<>(accountService.balanceSummary(form));
    }

    /** 净资产概览：只统计 include=true 的账户，按本位币折算（总资产/总负债/净资产） */
    @GetMapping("/netWorth")
    public SimpleResponse netWorth() {
        return new DataResponse<>(accountService.netWorth());
    }

    /** 资产/负债构成分布：include=true 且余额非 0 的账户，按本位币折算后降序（概览页饼图） */
    @GetMapping("/balanceDistribution")
    public SimpleResponse balanceDistribution() {
        return new DataResponse<>(accountService.balanceDistribution());
    }

    /** 余额调整：目标余额覆盖。走账户模块端点（非 MoneyFlow.create），服务内部写一条 ADJUST 流水历史。 */
    @PostMapping("/{id}/adjust")
    public SimpleResponse adjustBalance(@PathVariable("id") Long id, @Valid @RequestBody AdjustBalanceAddForm form) {
        accountService.adjustBalance(id, form);
        return new SimpleResponse();
    }

}
