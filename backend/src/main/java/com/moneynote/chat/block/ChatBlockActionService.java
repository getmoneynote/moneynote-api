package com.moneynote.chat.block;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.account.Account;
import com.moneynote.account.AccountRepository;
import com.moneynote.book.Book;
import com.moneynote.book.BookService;
import com.moneynote.chat.Message;
import com.moneynote.chat.MessageRepository;
import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyService;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.moneyflow.MoneyFlowCreateForm;
import com.moneynote.moneyflow.MoneyFlowService;
import com.moneynote.user.CurrentUserHolder;

/**
 * 表单块提交动作服务：把「用户在消息里填完表单点提交」变成一次真实的业务写入。
 *
 * <p><b>它与流式管线完全无关</b>：运行在普通请求线程上（{@code CurrentUserHolder} 可用），
 * 一个事务内完成「校验块状态 → 执行动作 → 回写块状态」。之所以不放进 Agent 工具：
 * 写库方法绝不注册给 LLM（R6），模型只能造表单，落库必须由用户点击触发。
 *
 * <p><b>防重复提交</b>：提交前对消息行加悲观写锁（见
 * {@link MessageRepository#findForUpdate}），块状态从 PENDING 改成 DONE 与账单写入同事务，
 * 双击/多标签页并发提交只会成功一次，也不会出现「账记了但块还显示待提交」。
 *
 * <p><b>金额口径</b>：表单里的金额是<b>账本币种主单位</b>（用户看到什么就提交什么），
 * 这里按账本币种精度换算成最小单位；若用户选了别的币种的账户，则按汇率换算出账户原币金额
 * 并同时给出账本币种的换算金额（与 {@code MoneyFlowService} 的 amount/convertedAmount 语义对齐）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBlockActionService {

    /** 表单块动作只支持支出/收入（转账要转出+转入两个账户，本版不做） */
    private static final int TYPE_EXPENSE = 1;
    private static final int TYPE_INCOME = 2;

    /** createTime 合法区间，与 MoneyFlowCreateForm 的 @Min/@Max 一致（3000-01-01 为上限） */
    private static final long MIN_CREATE_TIME = 0L;
    private static final long MAX_CREATE_TIME = 32503680000000L;

    private final MessageRepository messageRepository;
    private final BookService bookService;
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;
    private final MoneyFlowService moneyFlowService;

    /**
     * 提交一个表单块。
     *
     * @param agentCode 分区键（块 id 只在消息内唯一，必须与消息一起校验）
     * @param messageId 块所属消息
     * @param blockId   块 id（如 b1）
     * @param values    用户提交的字段值，覆盖在块自带的预填值上（可只传改动的字段）
     * @return 更新后的块（status=done + resultText），供前端就地刷新
     * @throws ResourceInvalidException 消息/块不存在、块不是表单、块已提交、动作不支持、字段不合法
     */
    @Transactional
    public MessageBlock submit(String agentCode, Long messageId, String blockId,
                               Map<String, Object> values) {
        Long userId = CurrentUserHolder.get();

        Message message = messageRepository.findForUpdate(messageId, userId, agentCode)
                .orElseThrow(() -> new ResourceInvalidException("error.chat.block.not.found"));
        List<MessageBlock> blocks = MessageBlocks.fromJson(message.getParts());
        MessageBlock block = blocks == null ? null : blocks.stream()
                .filter(item -> blockId.equals(item.getId()))
                .findFirst()
                .orElse(null);
        if (block == null) {
            throw new ResourceInvalidException("error.chat.block.not.found");
        }
        if (block.getType() != BlockType.FORM || !(block.getData() instanceof FormBlockData form)) {
            throw new ResourceInvalidException("error.chat.block.not.form");
        }
        if (form.getStatus() != FormStatus.PENDING) {
            // 已提交过（含并发提交的后到者）：拒绝而不是幂等返回，避免用户以为又记了一笔
            throw new ResourceInvalidException("error.chat.block.not.pending");
        }

        String resultText = switch (String.valueOf(form.getAction())) {
            case BlockActions.MONEY_FLOW_CREATE -> createMoneyFlow(form.getFields(), values, userId);
            default -> throw new ResourceInvalidException("error.chat.block.action.unsupported");
        };

        form.setStatus(FormStatus.DONE);
        form.setResultText(resultText);
        // 整个 parts 列表重新序列化：块状态是前台可见的事实，必须与账单同事务落库
        message.setParts(MessageBlocks.toJson(blocks));
        messageRepository.save(message);
        return block;
    }

    /**
     * money-flow.create：把表单字段落成一笔账单。
     *
     * <p>字段的权威定义在 {@code BookkeepingDraftTool}；这里只做「取值 + 校验 + 换算」，
     * 记账规则（分类归属、账户归属、余额变动）全部复用 {@link MoneyFlowService#create}。
     */
    private String createMoneyFlow(Map<String, Object> fields, Map<String, Object> values, Long userId) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (fields != null) {
            merged.putAll(fields);
        }
        if (values != null) {
            merged.putAll(values);
        }

        // 收支方向固定为模型生成草稿时判定的值：只认块自带的 type，忽略提交值里的 type。
        // 表单里该字段是只读的，这里再兜一层——否则改个请求体就能把「支出」提交成「收入」
        int type = requireType(fields == null ? null : fields.get("type"));
        BigDecimal amount = requireAmount(merged.get("amount"));
        Long accountId = toLong(merged.get("accountId"));
        Long categoryId = toLong(merged.get("categoryId"));
        Long createTime = toLong(merged.get("createTime"));
        String title = toText(merged.get("title"));
        String notes = toText(merged.get("notes"));

        Book book = bookService.resolveBook(null);
        Currency bookCurrency = book.getCurrency();
        int bookDecimalPlaces = bookCurrency.getDecimalPlaces();
        // 主单位 → 账本币种最小单位：按币种精度显式定标，零小数位币种也不会错 100 倍
        long bookMinor = toMinor(amount, bookDecimalPlaces);

        MoneyFlowCreateForm form = new MoneyFlowCreateForm();
        form.setBookId(book.getId());
        form.setType(type);
        form.setTitle(title);
        form.setNotes(notes);
        form.setCreateTime(isValidTime(createTime) ? createTime : Instant.now().toEpochMilli());
        form.setAccountId(accountId);
        form.setCategoryId(categoryId);
        // 用户点了提交即视为确认：这是「用户主动确认」的入口，与页面表单默认值一致
        form.setConfirm(true);
        form.setInclude(true);
        if (accountId == null) {
            // 未归属账户：金额按账本币种计量，不产生余额变动
            form.setAmount(bookMinor);
        } else {
            Account account = accountRepository.findByIdAndUserId(accountId, userId)
                    .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
            if (account.getCurrency().getId().equals(bookCurrency.getId())) {
                form.setAmount(bookMinor);
            } else {
                // 账户币种与账本币种不同：amount 是账户原币、convertedAmount 是账本币种，
                // 与页面表单「两个金额」的语义一致，避免把账本币种金额当成账户原币记进账户
                form.setAmount(currencyService.convert(bookMinor, bookCurrency, account.getCurrency()));
                form.setConvertedAmount(bookMinor);
            }
        }

        moneyFlowService.create(form);
        return "已记账：" + typeName(type) + " " + (title == null ? "" : title + " ")
                + amount.toPlainString() + " " + bookCurrency.getCode();
    }

    /** 主单位 → 最小单位；超出 Long 范围（明显是误填）按表单不合法处理，不做静默截断 */
    private static long toMinor(BigDecimal amount, int decimalPlaces) {
        try {
            return amount.setScale(decimalPlaces, RoundingMode.HALF_UP)
                    .movePointRight(decimalPlaces)
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new ResourceInvalidException("error.chat.block.form.invalid");
        }
    }

    private static int requireType(Object value) {
        Long type = toLong(value);
        if (type == null || (type != TYPE_EXPENSE && type != TYPE_INCOME)) {
            throw new ResourceInvalidException("error.chat.block.form.invalid");
        }
        return type.intValue();
    }

    /** 金额必须是正数：符号由 type（支出/收入）表达，负数会让余额方向与用户预期相反 */
    private static BigDecimal requireAmount(Object value) {
        if (!(value instanceof Number number)) {
            throw new ResourceInvalidException("error.chat.block.form.invalid");
        }
        // 经字符串构造：JSON 反序列化可能给出 Double，直接 new BigDecimal(double) 会带二进制尾巴
        BigDecimal amount = new BigDecimal(String.valueOf(number));
        if (amount.signum() <= 0) {
            throw new ResourceInvalidException("error.chat.block.form.invalid");
        }
        return amount;
    }

    private static String typeName(int type) {
        return type == TYPE_EXPENSE ? "支出" : "收入";
    }

    private static boolean isValidTime(Long time) {
        return time != null && time >= MIN_CREATE_TIME && time <= MAX_CREATE_TIME;
    }

    /** 数字/数字字符串 → Long：块字段来自 JSON，可能是 Integer/Long/Double/String */
    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new ResourceInvalidException("error.chat.block.form.invalid");
        }
    }

    private static String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

}
