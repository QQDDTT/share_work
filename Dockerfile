# 使用官方的 openjdk 基础镜像
FROM openjdk:17-jdk-alpine


# 设置工作目录
WORKDIR /home/nick/apps/share_work/

# 复制 Maven 项目文件到容器中
# COPY . .
COPY target/*.jar app.jar

# 创建所需的目录
RUN mkdir -p /home/json /home/test /home/logs

# 复制User json到容器中的指定目录
COPY users.json /home/json/users.json
RUN chmod 777 /home/json/users.json

# 设置权限
RUN chmod 777 /home/test /home/logs

# 暴露应用端口
EXPOSE 8080

# 运行 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "app.jar"]
