package com.moneynote.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页数据响应，{@link DataResponse} 的 data 为 {@link PageData}：
 * content 是当前页数据列表，并携带分页元信息（当前页码、每页条数、总条数）。
 *
 * <p>分页元信息放在 data 内部而不是顶层平铺，是为了让前端
 * 「统一取响应体 data 字段」的解包约定对所有接口成立，分页接口不再是特例。
 *
 * <p>直接从 Spring Data 的 {@link Page} 构造。
 * 注意 current 对外从 1 开始（Spring Data 的 pageNumber 从 0 开始，构造时已 +1）。
 *
 * @param <T> 列表中单个元素的类型
 */
@Getter @Setter
@NoArgsConstructor
public class PageResponse<T> extends DataResponse<PageResponse.PageData<T>> {

    public PageResponse(Page<T> page) {
        super(new PageData<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements()));
    }

    /**
     * 分页数据载荷：当前页列表 + 分页元信息。
     *
     * @param <T> 列表中单个元素的类型
     */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageData<T> {

        /** 当前页数据列表 */
        private List<T> content;
        /** 当前页码，从 1 开始 */
        private int current;
        /** 每页条数 */
        private int pageSize;
        /** 满足查询条件的总条数 */
        private long total;

    }

}
