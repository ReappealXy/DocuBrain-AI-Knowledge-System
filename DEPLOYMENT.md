# 🚀 DocuBrain 部署指南

'test文件目录下面有一些基本测试，也可以参考'
所有注释都是AI生成的，仅供参考。
本文档提供了 DocuBrain 智能知识库系统在不同环境下的部署指南。

## 📋 部署前准备

### 系统要求
- ☕ **Java 17+**
- 🐳 **Docker & Docker Compose**（推荐）在linux里面运行的话不支持centos7，版本太老了，不适配redis-stack,因此我在windows上构建的
- 🗄️ **MySQL 8.0+**
- 🔧 **Maven 3.6+**
- 💾 **至少 2GB 内存**
- 💿 **至少 10GB 磁盘空间**

### 环境变量配置

创建 `.env` 文件并配置以下变量：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=docubrain_db
DB_USERNAME=docubrain_user
DB_PASSWORD=your_secure_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# OpenAI API 配置
OPENAI_BASE_URL=https://api.openai.com
OPENAI_API_KEY=your_openai_api_key

# 应用配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# 安全配置
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_KEY=your_encryption_key
```

## 🐳 Docker 部署（推荐）

### 1. 创建 Docker Compose 文件

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: docubrain-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/sql:/docker-entrypoint-initdb.d
    networks:
      - docubrain-network

  # Redis Stack
  redis:
    image: redis/redis-stack:latest
    container_name: docubrain-redis
    environment:
      REDIS_ARGS: "--requirepass ${REDIS_PASSWORD}"
    ports:
      - "6379:6379"
      - "8001:8001"
    volumes:
      - redis_data:/data
    networks:
      - docubrain-network

  # DocuBrain 应用
  app:
    build: .
    container_name: docubrain-app
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - OPENAI_BASE_URL=${OPENAI_BASE_URL}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    ports:
      - "${SERVER_PORT}:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - docubrain-network
    volumes:
      - app_uploads:/app/uploads

volumes:
  mysql_data:
  redis_data:
  app_uploads:

networks:
  docubrain-network:
    driver: bridge
```

### 2. 创建 Dockerfile

创建 `Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 包装器
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 下载依赖（利用 Docker 缓存）
RUN ./mvnw dependency:go-offline

# 复制源代码
COPY src/ src/

# 构建应用
RUN ./mvnw clean package -DskipTests

# 创建上传目录
RUN mkdir -p /app/uploads

# 暴露端口
EXPOSE 8080

# 启动应用
CMD ["java", "-jar", "target/DocuBrain-0.0.1-SNAPSHOT.jar"]
```

### 3. 部署命令

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

## 🖥️ 传统部署

### 1. 数据库准备

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE docubrain_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'docubrain_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON docubrain_db.* TO 'docubrain_user'@'%';
FLUSH PRIVILEGES;

# 导入数据库结构
mysql -u docubrain_user -p docubrain_db < src/main/resources/sql/docubrain_db.sql
```

### 2. Redis 安装

```bash
# 使用 Docker 启动 Redis Stack
docker run -d \
  --name redis-stack \
  -p 6379:6379 \
  -p 8001:8001 \
  -e REDIS_ARGS="--requirepass your_redis_password" \
  redis/redis-stack:latest
```

### 3. 应用构建与启动

```bash
# 构建应用
mvn clean package -DskipTests

# 启动应用
java -jar target/DocuBrain-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/docubrain_db \
  --spring.datasource.username=docubrain_user \
  --spring.datasource.password=your_secure_password \
  --spring.data.redis.host=localhost \
  --spring.data.redis.port=6379 \
  --spring.data.redis.password=your_redis_password \
  --spring.ai.openai.api-key=your_openai_api_key
```

## 🔧 生产环境配置

### 1. 创建生产配置文件

创建 `src/main/resources/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:docubrain_db}?useSSL=true&requireSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

  ai:
    openai:
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7
          max-tokens: 1000
      embedding:
        options:
          model: text-embedding-3-small

logging:
  level:
    root: INFO
    xin.rexy.docubrain: INFO
  file:
    name: logs/docubrain.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

server:
  port: ${SERVER_PORT:8080}
  compression:
    enabled: true
  http2:
    enabled: true
```

### 2. 安全配置

- 🔐 使用强密码
- 🛡️ 启用 HTTPS
- 🚫 禁用不必要的端口
- 📊 配置监控和日志
- 🔄 定期备份数据

## 📊 监控与维护

### 健康检查

```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 检查数据库连接
curl http://localhost:8080/actuator/health/db

# 检查 Redis 连接
curl http://localhost:8080/actuator/health/redis
```

### 日志管理

```bash
# 查看应用日志
tail -f logs/docubrain.log

# 查看 Docker 日志
docker-compose logs -f app
```

### 备份策略

```bash
# 数据库备份
mysqldump -u docubrain_user -p docubrain_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Redis 备份
docker exec docubrain-redis redis-cli --rdb /data/backup_$(date +%Y%m%d_%H%M%S).rdb
```

## 🚨 故障排除

### 常见问题

1. **应用启动失败**
   - 检查 Java 版本
   - 验证数据库连接
   - 确认 Redis 服务状态

2. **向量搜索失败**
   - 确认 Redis Stack 正确安装
   - 检查 RediSearch 模块状态

3. **AI 接口调用失败**
   - 验证 API 密钥
   - 检查网络连接
   - 确认配额限制

### 性能优化

- 🔧 调整 JVM 参数
- 📊 优化数据库索引
- 🚀 配置 Redis 缓存
- 📈 监控系统资源

## 📞 技术支持

如果在部署过程中遇到问题，请：

1. 📋 检查日志文件
2. 🔍 查看 GitHub Issues
3. 📧 联系技术支持
4. 💬 加入社区讨论

---

*祝你部署顺利！🎉*