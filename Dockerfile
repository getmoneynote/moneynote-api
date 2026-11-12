# 构建上下文为仓库根目录（docker build 在根目录执行）
# 前端在镜像内实时构建，保证镜像永远使用最新 web-pc 产物

# ========== 前端构建阶段 ==========
FROM node:22-alpine AS fe-builder
WORKDIR /fe
RUN npm install -g pnpm@9
COPY web-pc/package.json web-pc/pnpm-lock.yaml ./
# --ignore-scripts：此时 config/src 尚未拷贝，跳过 prepare(max setup)；
# max build 会自动生成所需临时文件
RUN --mount=type=cache,id=pnpm-store,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile --ignore-scripts
COPY web-pc ./
RUN pnpm build

# ========== 后端构建阶段 ==========
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY backend/src src
# 用最新前端产物替换仓库中预构建的旧资源（umi 产物带 hash 文件名，必须先清空再拷贝）
RUN rm -rf src/main/resources/public
COPY --from=fe-builder /fe/dist src/main/resources/public

RUN ./mvnw clean package -DskipTests

# ========== 运行阶段 ==========
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 容器内保护机制:entrypoint.sh 作为 PID 1 循环拉起 java,
# 处理"配置保存后退出码 88 自动重启"与"被 kill/崩溃自动拉起"
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 9096
