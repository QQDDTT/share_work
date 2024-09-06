# 使用官方的 OpenJDK 17 Alpine 镜像作为基础镜像
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 将 Spring Boot 项目的 JAR 文件复制到容器中
COPY target/*.war app.war

# 暴露端口
EXPOSE 8080

# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.war"]
