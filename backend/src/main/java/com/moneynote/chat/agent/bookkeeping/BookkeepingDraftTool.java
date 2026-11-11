package com.moneynote.chat.agent.bookkeeping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.moneynote.chat.block.BlockActions;
import com.moneynote.chat.block.BlockCollector;
import com.moneynote.chat.block.BlockType;
import com.moneynote.chat.block.FormBlockData;
import com.moneynote.chat.block.FormStatus;

/**
 * 记账草稿表单工具：把模型从用户话里提取的记账要素包成一个「待确认表单」块。
 *
 * <p><b>不写库</b>（R6：写库方法绝不注册给 LLM）。工具只生产展示块，真正的落库发生在用户
 * 在表单里点击提交之后，由请求线程上的 {@code ChatBlockActionService} 调
 * {@code MoneyFlowService.create} 完成——模型一次幻觉最多画出一张表单，动不了账。
 *
 * <p>金额口径：{@code amount} 是<b>账本币种主单位</b>（元/美元），不是最小单位。主单位 → 最小单位
 * 的换算由提交端点按账本币种精度完成，模型不需要关心小数位。
 *
 * <p>账户与分类<b>故意留空</b>：模型看不到用户的账户/分类 id，硬猜只会猜错。这两项由用户在
 * 表单 UI 里选（前端走现有 options / tree 接口），提交端点再校验归属。
 *
 * <p>线程：在流管线线程上执行，{@link BlockCollector} 与时间基准都由构造参数带入。
 */
public class BookkeepingDraftTool {

    /** MoneyFlow 的流水类型：1=支出 2=收入（本版不含转账，转账要转出/转入两个账户，留给后续） */
    private static final int TYPE_EXPENSE = 1;
    private static final int TYPE_INCOME = 2;

    /** 模型可能给出的书面时间格式（按顺序尝试），换算在 Java 侧做，模型不必算毫秒 */
    private static final List<DateTimeFormatter> CREATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

    private final BlockCollector blocks;
    private final Instant now;

    public BookkeepingDraftTool(BlockCollector blocks, Instant now) {
        this.blocks = blocks;
        this.now = now;
    }

    @Tool(name = "stageBookkeepingDraft", description = """
            把用户描述的一笔收支整理成「记账确认表单」展示给用户，等用户核对/补全后自己提交。
            只要用户表达了记账意图（说明或暗示花了/收到多少钱、买了什么），就调用本工具，
            不要只把要素写在正文里。
            参数：
            - type：1=支出 2=收入（必填，本版不支持转账，转账请让用户去账单页面记）。
              该值在表单里是只读的、用户改不了，所以必须由你判准：花了/买了/付了是支出，
              收到/发工资/报销到账是收入；两种都讲得通时先追问，不要硬猜
            - amount：金额，账本币种主单位（如 35.5 元就传 35.5），必须为正数
            - title：一句话摘要，如「午餐」「打车」「工资」
            - notes：用户额外说明的补充信息，可空
            - occurredAt：发生的日期时间，格式 yyyy-MM-dd HH:mm，可空；用户说的是「昨天」「上周五」
              这类相对时间时换算成具体日期再传，用户没提时间就不传（表单默认当前时刻）
            注意（必须遵守）：
            1) 表单已由系统展示，回复里不要再重复罗列字段或再画一遍表单，用一两句话请用户核对提交即可；
            2) 严禁声称「已经记好了」「已记账」——记账要等用户在表单里点提交；
            3) 账户与分类由用户在表单里选择，你不要猜，也不要在正文里替用户决定；
            4) 金额或收支方向不明确时不要调用本工具，先追问缺失项。
            """)
    public String stageBookkeepingDraft(
            @ToolParam(description = "流水类型：1=支出，2=收入") Integer type,
            @ToolParam(description = "金额，账本币种主单位，必须为正数") BigDecimal amount,
            @ToolParam(description = "一句话摘要，如「午餐」「打车」") String title,
            @ToolParam(description = "补充说明，可空") String notes,
            @ToolParam(description = "发生时间，格式 yyyy-MM-dd HH:mm，可空", required = false)
            String occurredAt) {

        if (type == null || (type != TYPE_EXPENSE && type != TYPE_INCOME)) {
            return "未展示表单：type 只能是 1（支出）或 2（收入）。转账请让用户去账单页面记录。";
        }
        if (amount == null || amount.signum() <= 0) {
            return "未展示表单：金额缺失或不合法。请先向用户确认具体金额。";
        }
        String summaryTitle = (title == null || title.isBlank()) ? "一笔" + (type == TYPE_EXPENSE ? "支出" : "收入")
                : title.trim();

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("type", type);
        fields.put("amount", amount);
        fields.put("title", summaryTitle);
        fields.put("notes", notes == null || notes.isBlank() ? null : notes.trim());
        fields.put("createTime", resolveCreateTime(occurredAt));
        // 账户/分类留空：模型不知道 id，由用户在表单里选
        fields.put("accountId", null);
        fields.put("categoryId", null);

        FormBlockData data = new FormBlockData();
        data.setAction(BlockActions.MONEY_FLOW_CREATE);
        data.setTitle("记账确认");
        data.setFields(fields);
        data.setStatus(FormStatus.PENDING);

        String typeName = type == TYPE_EXPENSE ? "支出" : "收入";
        blocks.add(BlockType.FORM, buildSummary(typeName, amount, summaryTitle), data);

        return "已向用户展示「记账确认」表单：" + typeName + " " + amount.toPlainString()
                + "（账本币种），摘要「" + summaryTitle + "」。"
                + "请用一两句话请用户核对并提交，不要复述字段，也不要声称已经记账。";
    }

    /** 模型视角摘要：历史回读时拼进模型输入，让模型知道这条回复当时展示过什么表单 */
    private static String buildSummary(String typeName, BigDecimal amount, String title) {
        return "[表单] 记账确认：" + typeName + " " + title + " " + amount.toPlainString()
                + "（账本币种），等待用户核对提交";
    }

    /**
     * 发生时间：模型传的是「yyyy-MM-dd HH:mm」书面时间（也接受纯日期），换算成 Unix 毫秒。
     * 解析失败或未传一律回落到本条消息的发送时刻——时间错了用户在表单里改得回来，
     * 但让模型自己算毫秒一定会算错，所以参数只收可读时间、换算在 Java 侧做。
     */
    private long resolveCreateTime(String occurredAt) {
        if (occurredAt == null || occurredAt.isBlank()) {
            return now.toEpochMilli();
        }
        String text = occurredAt.trim();
        for (DateTimeFormatter formatter : CREATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter).atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // 换下一种格式继续尝试；全部失败时回落当前时刻
            }
        }
        try {
            return LocalDate.parse(text).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return now.toEpochMilli();
        }
    }

}
