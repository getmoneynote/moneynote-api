package com.moneynote.exception;

/*
401 vs 403 的区分:
如果以后有"已登录但无权限访问某资源"的场景(比如访问别人的 currency),那是 403 Forbidden,
和"根本没登录/token 失效"的 401 是两个不同的异常类型,
建议提前想好要不要拆成 UnauthorizedException(401,未认证)和 ForbiddenException(403,已认证但无权限)两个类,
现在 findByIdAndUserId 返回空时抛的其实更接近"资源不存在"(404,你之前用的 ResourceNotFoundException),这个划分是对的,不用改。

token 过期和 token 无效是否要区分:
目前"没传 token"与"token 查不到对应 userId"都抛本异常,统一渲染 error.auth.token.missing("请先登录")。
如果前端需要区分"从未登录"和"登录过期"两种不同提示文案,可以改为分别传
error.auth.token.missing 和 error.auth.token.invalid,但状态码都还是 401,这个看你产品需求,不是必须的。
 */
public class UnauthorizedException extends BusinessException {

    private static final String DEFAULT_KEY = "error.auth.token.missing";

    public UnauthorizedException() {
        super(DEFAULT_KEY);
    }

    public UnauthorizedException(String messageKey) {
        super(messageKey);
    }

}
