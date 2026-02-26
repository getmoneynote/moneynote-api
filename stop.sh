#!/bin/bash

JAR_NAME="moneynote-api-1.0.0.jar"

# 1. 寻找正在运行的进程 ID
PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "⚠️ 未发现正在运行的 $JAR_NAME 进程。"
else
    echo "停止进程 PID: $PID ..."
    # 2. 先尝试正常停止 (SIGTERM)，让 Spring 有时间做善后工作
    kill $PID

    # 3. 等待最多 10 秒，检查进程是否真的消失了
    count=0
    while [ $count -lt 10 ]; do
        if ps -p $PID > /dev/null; then
            sleep 1
            ((count++))
        else
            echo "✅ 程序已成功停止。"
            exit 0
        fi
    done

    # 4. 如果 10 秒后还没停止，强制杀掉 (SIGKILL)
    echo "程序未能在 10 秒内停止，正在强制关闭..."
    kill -9 $PID
    echo "✅ 程序已强制停止。"
fi