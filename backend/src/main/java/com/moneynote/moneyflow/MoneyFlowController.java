package com.moneynote.moneyflow;

import com.moneynote.response.DataResponse;
import com.moneynote.response.PageResponse;
import com.moneynote.response.SimpleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/money-flows")
@RequiredArgsConstructor
public class MoneyFlowController {

    private final MoneyFlowService moneyFlowService;

    @PostMapping
    public SimpleResponse create(@Valid @RequestBody MoneyFlowCreateForm form) {
        moneyFlowService.create(form);
        return new SimpleResponse();
    }

    @PatchMapping("/{id}")
    public SimpleResponse update(@PathVariable("id") Long id, @Valid @RequestBody MoneyFlowCreateForm form) {
        moneyFlowService.update(id, form);
        return new SimpleResponse();
    }

    @DeleteMapping("/{id}")
    public SimpleResponse delete(@PathVariable("id") Long id) {
        moneyFlowService.delete(id);
        return new SimpleResponse();
    }

    @GetMapping
    public SimpleResponse list(
            MoneyFlowQueryForm form,
            @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable page) {
        return new PageResponse<>(moneyFlowService.query(form, page));
    }

    /** 与列表同一套过滤条件（忽略 type），返回总支出/总收入/结余。 */
    @GetMapping("/statistics")
    public SimpleResponse statistics(MoneyFlowQueryForm form) {
        return new DataResponse<>(moneyFlowService.statistics(form));
    }

    /**
     * 分类统计（概览页支出/收入分类环形图）：与列表同一套过滤条件，
     * type 必填且仅限 1=支出 / 2=收入；按根分类汇总，金额折算本位币。
     */
    @GetMapping("/category-statistics")
    public SimpleResponse categoryStatistics(MoneyFlowQueryForm form) {
        return new DataResponse<>(moneyFlowService.categoryStatistics(form));
    }
}