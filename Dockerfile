FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 复制 Maven 包装器
COPY mvnw* ./
COPY .mvn .mvn

# 复制项目文件
COPY pom.xml .
COPY src ./src

# 构建应用
RUN chmod +x mvnw && ./mvnw package -DskipTests

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "target/pharmacy-mall-1.0.0.jar"]
