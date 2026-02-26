#!/bin/bash

ENV_FILE="moneynote.env"
JAR_NAME="moneynote-api-1.0.0.jar"
LOG_FILE="app.log"

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ 错误: 找不到 $ENV_FILE"
    exit 1
fi

# 1. 提取变量
DB_HOST=$(grep DB_HOST $ENV_FILE | cut -d '=' -f2 | tr -d '\r')
DB_PORT=$(grep DB_PORT $ENV_FILE | cut -d '=' -f2 | tr -d '\r')
DB_NAME=$(grep DB_NAME $ENV_FILE | cut -d '=' -f2 | tr -d '\r')
DB_USER=$(grep DB_USER $ENV_FILE | cut -d '=' -f2 | tr -d '\r')
DB_PASSWORD=$(grep DB_PASSWORD $ENV_FILE | cut -d '=' -f2 | tr -d '\r')

# 2. 清空旧日志文件
if [ -f "$LOG_FILE" ]; then
    cat /dev/null > "$LOG_FILE"
    echo "🧹 已清空旧日志文件: $LOG_FILE"
fi

echo "🚀 正在后台启动 $JAR_NAME..."

# 3. 使用 nohup 运行，注意这里的 > 表示覆盖写入
nohup java -DDB_HOST=$DB_HOST \
     -DDB_PORT=$DB_PORT \
     -DDB_NAME=$DB_NAME \
     -DDB_USER=$DB_USER \
     -DDB_PASSWORD=$DB_PASSWORD \
     -jar $JAR_NAME > $LOG_FILE 2>&1 &

echo "✅ 程序已在后台运行。旧日志已擦除，新日志将输出到: $LOG_FILE"