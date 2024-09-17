# 使用官方的 OpenJDK 17 Alpine 镜像作为基础镜像
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /home/nick/apps/share_work/

# 将 Spring Boot 项目的 JAR 文件复制到容器中
COPY /target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]
