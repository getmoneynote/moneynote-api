package com.moneynote.chat.agent.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;

import com.moneynote.chat.block.BlockCollector;
import com.moneynote.chat.block.BlockType;
import com.moneynote.chat.block.ChartBlockData;

/**
 * 资产分布图表工具：把账户快照确定性地映射成饼图块，交给 {@link BlockCollector}。
 *
 * <p>为什么与 {@link AccountBalanceTool} 分开：取数（模型要不要数据）与出图（模型要不要图）
 * 是两个独立的意图判断——模型可能只想口头回答，也可能想看结构。合并会让「只问一个数字」
 * 也强制弹图。分开后取数工具保持纯净，出图工具自带取数。
 *
 * <p>图表的每个数字都由 Java 从快照映射（不经模型手写 JSON），模型只负责解读文字，
 * 这是「展示数据不过模型的手」的落地点。
 *
 * <p>线程同 {@link AccountBalanceTool}：在流管线线程上执行，身份与依赖全部来自构造参数。
 */
public class AccountChartTool {

    /** 单个账户的金额已由服务折算到本位币主单位，且只取资产类正余额账户 */
    private final AccountAnalysisService accountAnalysisService;
    private final Long userId;
    private final BlockCollector blocks;

    public AccountChartTool(AccountAnalysisService accountAnalysisService, Long userId,
                            BlockCollector blocks) {
        this.accountAnalysisService = accountAnalysisService;
        this.userId = userId;
        this.blocks = blocks;
    }

    @Tool(name = "showAssetDistributionChart", description = """
            向用户展示「资产分布」饼图（按账户拆分的资产类余额占比，金额已折算到本位币）。
            用户询问资产怎么分布、各账户占比、资产结构，或你打算用表格罗列多个账户金额时调用本工具。
            本工具内部已取数，调用前不必再调 getAccountBalances。
            返回：图表的标题、币种与关键数据摘要。
            调用后（必须遵守）：
            1) 图表已自动展示给用户，回复里不要再复述每个账户的金额表格，也不要用文字画图；
            2) 用 2-3 句话总结结构（最大项、集中度或明显特征）即可；
            3) 同一轮回答只调用一次，不要重复出图；
            4) 返回提示没有可展示的账户时，如实说明没有资产类账户，不要声称已展示图表。
            """)
    public String showAssetDistributionChart() {
        AccountAnalysisResponse snapshot = accountAnalysisService.snapshot(userId);

        // 只画资产结构：负债类账户是负数，放进饼图会把占比算歪（负债结构另有其表，本期不做）
        List<ChartBlockData.Item> items = new ArrayList<>();
        for (AccountAnalysisItem account : snapshot.getAccounts()) {
            if (!AccountAnalysisService.ASSET_TYPE_NAMES.contains(account.getTypeName())) {
                continue;
            }
            if (account.getBaseBalance() == null || account.getBaseBalance().signum() <= 0) {
                // 余额为 0/负数（如已刷爆的资产账户）不占份额，跳过
                continue;
            }
            items.add(new ChartBlockData.Item(account.getName(), account.getBaseBalance()));
        }
        if (items.isEmpty()) {
            return "当前没有可展示的资产类账户（可能没有账户、账户未计入净资产或余额为 0）。"
                    + "请如实说明看不到资产构成，不要编造数据，也不要声称已展示图表。";
        }

        String currencyCode = snapshot.getBaseCurrencyCode();
        BigDecimal total = items.stream()
                .map(ChartBlockData.Item::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String title = "资产分布";
        ChartBlockData data = new ChartBlockData("pie", title, currencyCode, items);
        blocks.add(BlockType.CHART, buildSummary(title, currencyCode, items, total), data);

        return "已向用户展示图表「" + title + "（" + currencyCode + "）」，共 " + items.size()
                + " 个资产账户，合计 " + total.toPlainString() + " " + currencyCode
                + "。明细：" + describe(items) + "。"
                + "请用 2-3 句话总结结构，不要再用表格复述这些金额。";
    }

    /**
     * 模型视角摘要：历史回读时拼进模型输入（见 ChatTransactionService.initialize）。
     * 用确定性代码生成，不额外调模型；读者是模型，所以写成一句可读的事实陈述。
     */
    private static String buildSummary(String title, String currencyCode,
                                       List<ChartBlockData.Item> items, BigDecimal total) {
        ChartBlockData.Item largest = items.stream()
                .max((a, b) -> a.value().compareTo(b.value()))
                .orElse(null);
        String largestPart = largest == null
                ? ""
                : "，最大为" + largest.name() + " " + largest.value().toPlainString();
        return "[图表] " + title + "（" + currencyCode + "）：" + describe(items)
                + "，合计 " + total.toPlainString() + " " + currencyCode + largestPart;
    }

    private static String describe(List<ChartBlockData.Item> items) {
        return items.stream()
                .map(item -> item.name() + " " + item.value().toPlainString())
                .collect(Collectors.joining("、"));
    }

}
