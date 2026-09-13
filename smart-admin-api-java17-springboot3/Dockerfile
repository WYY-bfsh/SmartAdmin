# ============ 构建阶段：用 Maven 镜像编译打包 ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 国内 Maven 镜像（阿里云）。不放这里就是直连 Maven Central，
# dependency:go-offline 阶段会偶发 EOF、或长时间无输出。
COPY settings.xml /root/.m2/settings.xml

# 先只复制 pom 文件下载依赖（利用 Docker 层缓存：改代码不用重新下载依赖）
COPY pom.xml .
COPY sa-base/pom.xml sa-base/
COPY sa-admin/pom.xml sa-admin/
RUN mvn -B -q dependency:go-offline || true

# 再复制源码进行打包（-Pprod 激活生产配置）
COPY sa-base/src sa-base/src
COPY sa-admin/src sa-admin/src
RUN mvn -B clean package -DskipTests -Pprod \
    && ls sa-admin/target/*.jar | grep -v original | xargs -I{} cp {} /build/app.jar

# ============ 运行阶段：只带 JRE，镜像更小 ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV TZ=Asia/Shanghai
COPY --from=builder /build/app.jar app.jar
EXPOSE 1024
# MaxRAMPercentage 让 JVM 根据容器内存限制自动分配堆，避免吃光服务器内存
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar", "--spring.profiles.active=prod"]
