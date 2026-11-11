package com.moneynote.chat.block;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 表单块提交请求体：{@code values} 是用户在表单里填的字段值，键与 {@link FormBlockData#getFields()}
 * 的预填值一致（type / amount / title / notes / createTime / accountId / categoryId）。
 *
 * <p>同名字段覆盖预填值，因此前端只回传改动过的字段也能提交；金额为账本币种主单位。
 * 服务端不信任这些值，一律重新校验（见 {@link ChatBlockActionService}）。
 */
@Getter
@Setter
public class MessageBlockSubmitForm {

    private Map<String, Object> values;

}
