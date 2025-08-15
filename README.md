# 🧠 基于springAI 的 DocuBrain 智能知识库问答系统

> 🚀 基于 Spring AI 和 RAG 技术的现代化智能知识库系统

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.1-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Redis Stack](https://img.shields.io/badge/Redis%20Stack-latest-red.svg)](https://redis.io/docs/stack/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介

DocuBrain 是一个基于 **RAG（检索增强生成）** 技术的智能知识库问答系统，旨在帮助用户高效管理和检索文档知识。系统支持多种文档格式上传，通过向量化存储和智能检索，为用户提供精准的问答服务。

### 🎯 核心价值

- **📚 智能文档管理**：支持 PDF、Word、TXT 等多种格式文档上传和解析
- **🔍 精准知识检索**：基于向量相似度的智能检索，快速定位相关内容
- **🤖 智能问答生成**：结合检索结果和大语言模型，生成准确的答案
- **👥 多用户支持**：完整的用户权限管理和知识库隔离
- **🎨 现代化界面**：基于 Thymeleaf 的响应式前端设计

## 🏗️ 技术架构

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 3.3.6 | 应用框架 |
| **Spring AI** | 1.0.1 | AI 集成框架 |
| **Spring Security** | 6.x | 安全认证 |
| **MyBatis-Plus** | 3.5.7 | ORM 框架 |
| **MySQL** | 8.0+ | 关系数据库 |
| **Redis Stack** | latest | 向量数据库 |
| **Apache Tika** | - | 文档解析 |
| **OpenAI API** | - | 大语言模型 |

### 前端技术栈

- **Thymeleaf**：服务端模板引擎
- **Bootstrap**：响应式 UI 框架
- **JavaScript**：前端交互逻辑
- **Chart.js**：数据可视化

## 🚀 核心功能

### 📁 文档管理模块
- ✅ 多格式文档上传（PDF、DOC、DOCX、TXT）（只测试了txt,其他的没试）
- ✅ 文档内容自动解析和分块
- ✅ 文档向量化存储
- ✅ 文档删除和管理

### 🤖 智能问答模块
- ✅ 基于向量相似度的文档检索
- ✅ RAG 技术结合检索和生成
- ✅ 实时问答交互
- ✅ 问答历史记录

### 👤 用户权限模块
- ✅ 用户注册和登录
- ✅ 基于 Spring Security 的安全认证
- ✅ 知识库权限隔离
- ✅ 用户会话管理

### ⚙️ 系统配置模块
- ✅ 知识库创建和管理
- ✅ 系统参数配置
- ✅ API 文档集成（Knife4j）
- ✅ 开发调试工具

## 📊 数据库设计

### 核心数据表

```sql
-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库表
CREATE TABLE knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档信息表
CREATE TABLE document_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    knowledge_base_id BIGINT,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 文档分块表
CREATE TABLE document_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT,
    chunk_index INT,
    content TEXT,
    vector_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🛠️ 快速开始

### 环境要求

- ☕ **Java 17+**
- 🐳 **Docker Desktop**（推荐）
- 🗄️ **MySQL 8.0+**
- 🔧 **Maven 3.6+**

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/your-username/DocuBrain.git
cd DocuBrain
```

#### 2. 启动 Redis Stack（推荐使用 Docker）
```bash
# 使用 Docker 启动 Redis Stack
docker run -d \
  --name redis-stack \
  -p 6379:6379 \
  -p 8001:8001 \
  redis/redis-stack:latest
```

#### 3. 配置数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE docubrain_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库结构
mysql -u root -p docubrain_db < src/main/resources/sql/docubrain_db.sql
```

#### 4. 配置应用

**方式一：使用环境变量（推荐）**

复制环境变量模板：
```bash
cp .env.example .env
```

编辑 `.env` 文件，填入你的配置：
```bash
# 数据库配置
DB_USERNAME=root
DB_PASSWORD=your_database_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# OpenAI API 配置
OPENAI_BASE_URL=https://api.openai.com
OPENAI_API_KEY=your_openai_api_key
```

**方式二：使用本地配置文件**

复制配置模板：
```bash
cp src/main/resources/application-example.yml src/main/resources/application-local.yml
```

编辑 `application-local.yml` 文件，填入你的实际配置。

**⚠️ 注意**：
- `.env` 和 `application-local.yml` 文件已添加到 `.gitignore`，不会被提交到 Git
- 请勿在 `application.yml` 中直接填写敏感信息

#### 5. 启动应用
```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者打包后启动
mvn clean package
java -jar target/DocuBrain-0.0.1-SNAPSHOT.jar
```

#### 6. 访问应用
- 🌐 **主页**：http://localhost:8080
- 💬 **聊天页面**：http://localhost:8080/chat
- 📚 **API 文档**：http://localhost:8080/doc.html
- 🔍 **Redis 管理**：http://localhost:8001

## 🎯 使用指南

### 📤 上传文档
1. 访问上传页面：http://localhost:8080/upload
2. 选择知识库或创建新知识库
3. 上传支持的文档格式（PDF、DOC、DOCX、TXT）
4. 系统自动解析并向量化存储

### 💬 智能问答
1. 访问聊天页面：http://localhost:8080/chat
2. 选择要查询的知识库
3. 输入问题，系统将：
   - 🔍 检索相关文档片段
   - 🤖 结合 AI 生成准确答案
   - 📋 显示引用来源

## 🐛 开发历程：一场史诗级的调试冒险

> 💡 这个项目的开发过程充满了挑战和学习，以下是我们遇到的主要问题和解决方案：

### 🎬 序章：神秘的 Bean 无法创建
**问题**：`UnsatisfiedDependencyException: required a bean of type 'EmbeddingModel'`

**解决**：理解了 Spring AI 的核心依赖关系，向量存储必须与嵌入模型配对使用。

### 🔍 第一章：寻找嵌入模型
**问题**：DeepSeek-Coder 模型已下架

**解决**：
- 使用 `curl` 命令独立测试 API 可用性
- 切换到 OpenAI 的 `text-embedding-3-small` 模型
- 学会了隔离问题的重要性

### ⚙️ 第二章：配置的陷阱
**问题**：一系列配置文件错误

**解决**：
- 修正 YAML 层级结构
- 解决 `base-url` 重复拼接问题
- 使用 `@SpringBootTest(excludeAutoConfiguration=...)` 优化测试

### 🏗️ 第三章：深入底层环境
**问题**：`Connection reset` - Redis 不支持向量搜索

**解决**：
- 发现需要 Redis Stack 而非普通 Redis
- CentOS 7 系统过旧，无法原生安装
- 最终采用 Docker 解决所有环境问题

### 🚀 第四章：功能深化
**问题**：从"能跑"到"正确"

**解决**：
- 实现完整的 RAG 功能
- 解决 API 版本兼容性问题
- 构建真正的智能问答系统

### 🔧 第五章：框架的深水区
**问题**：版本兼容性冲突

**解决**：
- 升级到 Spring Boot 3.3.6
- 使用 MyBatis-Plus Spring Boot 3 专用 starter
- 解决各种 Bean 注入和自动配置问题

### 🏆 最终成果
经过这一系列问题的解决，我们成功构建了一个：
- ✅ **功能完整**的 RAG 系统
- ✅ **代码规范**的 Spring Boot 应用
- ✅ **配置正确**的多技术栈集成
- ✅ **环境可靠**的容器化部署
- ✅ **版本兼容**的现代化技术栈

## 📚 相关文档

- 📋 **[部署指南](DEPLOYMENT.md)** - 详细的生产环境部署文档
- 🐛 **[已知问题](Bug.md)** - 当前版本的已知问题和解决方案

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📝 开发笔记

### 🔧 调试技巧
- 使用 `curl` 独立验证外部 API
- 分层排错：代码 → 配置 → 环境 → 基础设施
- 善用 Docker 解决环境依赖问题

### 📚 学习收获
- Spring AI 的架构和最佳实践
- RAG 技术的实际应用
- 微服务架构的依赖管理
- 现代化开发工具链的使用

### 🐛 已知问题

当前版本存在一些已知问题，详情请查看 **[Bug.md](Bug.md)**：

- **文档与知识库关联问题**：文档上传后可能绑定到错误的知识库
- **文档计数显示问题**：知识库显示的文档数量可能不准确

我们正在积极修复这些问题，欢迎提交 Issue 或 Pull Request 帮助改进！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - 强大的 AI 集成框架
- [OpenAI](https://openai.com/) - 优秀的大语言模型服务
- [Redis Stack](https://redis.io/docs/stack/) - 高性能向量数据库
- [Apache Tika](https://tika.apache.org/) - 文档解析利器

---

<div align="center">

**🌟 如果这个项目对你有帮助，请给个 Star！🌟**

*Made with ❤️ by DocuBrain Team*

</div>