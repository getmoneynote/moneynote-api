package com.moneynote.chat;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动清扫：进程重启/被杀时正在生成的消息永远停在 GENERATING
 * （complete/fail/stop 与 doOnCancel 都随 JVM 消失），启动时批量置为 ERROR，
 * 前端刷新后不再显示"生成中"。
 * cutoff 取清扫执行时刻，只处理此前创建的消息，避免误伤启动后刚到的新请求。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeneratingMessageSweepRunner implements ApplicationRunner {

    private final ChatTransactionService chatTransactionService;

    @Override
    public void run(ApplicationArguments args) {
        int swept = chatTransactionService.sweepStaleGenerating(Instant.now());
        if (swept > 0) {
            log.warn("启动清扫：{} 条 GENERATING 残留消息已置为 ERROR", swept);
        }
    }

}
