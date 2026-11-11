#!/usr/bin/env bash
# MoneyNote H2 在线备份（不需要停服务、不需要停容器）
#
# 用法：
#   ./scripts/backup-h2.sh
#   JAVA_BIN=/c/Users/markl/.jdks/openjdk-22.0.1/bin/java ./scripts/backup-h2.sh
#   BACKUP_DIR=/mnt/nas/moneynote ./scripts/backup-h2.sh      # 备份到别的盘/异地
#
# 可覆盖的环境变量（都有默认值）：
#   JAVA_BIN    java 可执行文件（默认依次找 JAVA_HOME、PATH、~/.jdks/*）
#   H2_JAR      h2-*.jar 路径（默认找 ~/.m2/repository，其次用 target/*.jar 里的 H2）
#   DATA_DIR    H2 库文件目录，默认 backend/data
#   CONFIG_DIR  配置目录，默认 backend/config
#   DB_NAME     库名，默认 moneynote
#   DB_USER / DB_PASSWORD   默认 sa / 空（与 config/db-config.properties 一致）
#   BACKUP_DIR  备份根目录，默认 backend/backups
#
# 每次生成 backup-<时间戳>/ 目录，内含 3 个文件：
#   moneynote.sql         逻辑全量：DDL + 数据，可读、可 diff、可迁移到其他数据库
#   moneynote.zip         H2 `BACKUP TO` 的事务一致性物理快照，恢复最快
#   db-config.properties  数据库配置（库名/账号密码），恢复时必须一起拷回
#
# 恢复（任选其一，务必先停掉应用/容器）：
#   A. 用 zip：停服务 -> 备份并移走当前 data/moneynote.mv.db ->
#      unzip moneynote.zip -d data/ -> 拷回 db-config.properties 到 config/ -> 启动
#   B. 用 sql：停服务 -> 移走当前 data/moneynote.mv.db（让库为空）-> 执行
#      java -cp "<h2.jar>" org.h2.tools.RunScript \
#        -url "jdbc:h2:file:E:/.../data/moneynote;MODE=MySQL;AUTO_SERVER=TRUE" \
#        -user sa -password "" -script moneynote.sql
#      -> 拷回 db-config.properties -> 启动
#
# 注意：
#   1. 不要在运行中直接 cp .mv.db，文件可能处于写入中间态，拷出来的副本会损坏。
#   2. 本脚本不删除任何旧备份，请自行定期清理 backups/ 下的历史目录。
#   3. 备份请放到与 data/ 不同的磁盘或机器，否则磁盘故障等于没有备份。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

DATA_DIR="${DATA_DIR:-$BACKEND_DIR/data}"
CONFIG_DIR="${CONFIG_DIR:-$BACKEND_DIR/config}"
BACKUP_ROOT="${BACKUP_DIR:-$BACKEND_DIR/backups}"
DB_NAME="${DB_NAME:-moneynote}"
DB_USER="${DB_USER:-sa}"
DB_PASSWORD="${DB_PASSWORD:-}"

die() { echo "[错误] $*" >&2; exit 1; }

# Git Bash 下 java 是 Windows 程序，路径需转成 C:/... 形式；Linux 原样返回
to_native() {
  if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else printf '%s' "$1"; fi
}

find_java() {
  if [ -n "${JAVA_BIN:-}" ]; then printf '%s' "$JAVA_BIN"; return; fi
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then printf '%s' "$JAVA_HOME/bin/java"; return; fi
  if command -v java >/dev/null 2>&1; then printf 'java'; return; fi
  local c
  for c in "$HOME"/.jdks/*/bin/java "$HOME"/.jdks/*/bin/java.exe; do
    if [ -x "$c" ]; then printf '%s' "$c"; return; fi
  done
  return 1
}

JAVA_BIN="$(find_java || true)"
[ -n "$JAVA_BIN" ] || die "找不到 java，请设置 JAVA_BIN（本机示例：JAVA_BIN=/c/Users/markl/.jdks/openjdk-22.0.1/bin/java）"

# 优先用独立的 H2 jar；没有则用 Spring Boot fat jar 里自带的 H2 + PropertiesLauncher
H2_CP=""
H2_MODE=""
if [ -n "${H2_JAR:-}" ]; then
  H2_CP="$H2_JAR"
  H2_MODE="jar"
else
  H2_CP="$(ls -1t "$HOME"/.m2/repository/com/h2database/h2/*/h2-*.jar 2>/dev/null | head -n 1 || true)"
  if [ -n "$H2_CP" ]; then
    H2_MODE="jar"
  else
    H2_CP="$(ls -1t "$BACKEND_DIR"/target/*.jar 2>/dev/null | head -n 1 || true)"
    if [ -n "$H2_CP" ]; then H2_MODE="fatjar"; fi
  fi
fi
[ -n "$H2_CP" ] || die "找不到 H2 jar，请设置 H2_JAR（例如 ~/.m2/repository/com/h2database/h2/2.4.240/h2-2.4.240.jar）"

[ -f "$DATA_DIR/$DB_NAME.mv.db" ] || die "数据库文件不存在：$DATA_DIR/$DB_NAME.mv.db"

run_h2() {
  local main="$1"; shift
  local cp; cp="$(to_native "$H2_CP")"
  if [ "$H2_MODE" = "fatjar" ]; then
    "$JAVA_BIN" -Dloader.main="$main" -cp "$cp" \
      org.springframework.boot.loader.launch.PropertiesLauncher "$@"
  else
    "$JAVA_BIN" -cp "$cp" "$main" "$@"
  fi
}

stamp="$(date +%Y%m%d-%H%M%S)"
out="$BACKUP_ROOT/backup-$stamp"
mkdir -p "$out"

db_path="$(to_native "$DATA_DIR/$DB_NAME")"
url="jdbc:h2:file:$db_path;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=0"
sql_out="$(to_native "$out/moneynote.sql")"
zip_out="$(to_native "$out/moneynote.zip")"

if [ -f "$DATA_DIR/$DB_NAME.lock.db" ]; then
  echo "[提示] 检测到 .lock.db（数据库正被进程占用），AUTO_SERVER 模式下可继续在线导出"
fi

echo "[1/4] 导出逻辑全量 SQL ..."
run_h2 org.h2.tools.Script \
  -url "$url" -user "$DB_USER" -password "$DB_PASSWORD" -script "$sql_out" >/dev/null

echo "[2/4] 生成一致性物理快照 ..."
run_h2 org.h2.tools.Shell \
  -url "$url" -user "$DB_USER" -password "$DB_PASSWORD" -sql "BACKUP TO '$zip_out'" >/dev/null

echo "[3/4] 复制数据库配置 ..."
if [ -f "$CONFIG_DIR/db-config.properties" ]; then
  cp "$CONFIG_DIR/db-config.properties" "$out/db-config.properties"
else
  echo "[警告] 未找到 $CONFIG_DIR/db-config.properties，恢复时需手工重建配置"
fi

echo "[4/4] 校验备份 ..."
[ -s "$out/moneynote.sql" ] || die "SQL 备份为空"
[ -s "$out/moneynote.zip" ] || die "ZIP 备份为空"
tables="$(grep -cE 'CREATE (CACHED |MEMORY )?TABLE' "$out/moneynote.sql" || true)"
[ "$tables" -gt 0 ] || die "SQL 备份里没有建表语句，导出可能失败"
if command -v unzip >/dev/null 2>&1; then
  unzip -t "$out/moneynote.zip" >/dev/null 2>&1 || die "ZIP 备份校验失败"
fi

echo
echo "[完成] $out  （表数量：$tables）"
ls -lh "$out"
echo
echo "提醒：备份目录请同步到与 data/ 不同的磁盘或机器；本脚本不会自动删除旧备份。"
