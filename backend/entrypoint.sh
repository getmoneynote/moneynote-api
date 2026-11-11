#!/bin/sh
# MoneyNote 容器入口脚本(作为容器 PID 1)
#
# 保护机制全部实现在容器内部,不依赖 compose 的 restart 策略:
#   1. 数据库配置向导保存后,应用以退出码 88 退出 -> 自动重启(走完整 Spring 启动流程)
#   2. 应用进程被 kill / 崩溃(OOM 等) -> 自动拉起
#   3. 收到 SIGTERM / SIGINT(docker stop / Ctrl-C) -> 优雅停止,不再拉起
#
# 注意: 若整个容器被 `docker kill`(SIGKILL)杀掉,容器内无法自救,
#       那需要容器层面的重启策略(Docker 级 restart)兜底。

RESTART_EXIT_CODE=88
RESTART_DELAY=5
JAVA_PID=""

# 优雅停止:把 TERM 转发给 java,等它走完 Spring 关闭流程(落盘/flush)再退出
on_signal() {
    echo "[entrypoint] 收到终止信号,正在优雅停止 java ..."
    if [ -n "${JAVA_PID}" ]; then
        kill -TERM "${JAVA_PID}" 2>/dev/null || true
        wait "${JAVA_PID}"
    fi
    echo "[entrypoint] 已退出"
    exit 0
}
trap 'on_signal' TERM INT

while :; do
    echo "[entrypoint] 启动 MoneyNote ..."
    java -XX:MaxRAMPercentage=75 -jar /app/app.jar &
    JAVA_PID=$!
    # 为什么不耗资源?
    wait "${JAVA_PID}"
    EXIT_CODE=$?
    echo "[entrypoint] 应用已退出,退出码: ${EXIT_CODE}"

    if [ "${EXIT_CODE}" -eq "${RESTART_EXIT_CODE}" ]; then
        echo "[entrypoint] 检测到数据库配置已更新(退出码 88),1 秒后自动重启..."
        sleep 1
    elif [ "${EXIT_CODE}" -eq 0 ]; then
        echo "[entrypoint] 应用正常退出(退出码 0),不再自动重启"
        exit 0
    else
        echo "[entrypoint] 应用异常退出(退出码 ${EXIT_CODE}),${RESTART_DELAY} 秒后自动拉起..."
        sleep "${RESTART_DELAY}"
    fi
done