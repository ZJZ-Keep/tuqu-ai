# Docker 镜像构建 - 使用华为云 Eclipse Temurin 21 镜像 (OpenJDK)

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/eclipse-temurin:21-jre

# 设置工作目录
WORKDIR /app

# 复制 JAR 包到容器中
COPY zjz-ai-agent-mcp-server-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口 (根据实际应用调整)
EXPOSE 8127

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=sse"]
