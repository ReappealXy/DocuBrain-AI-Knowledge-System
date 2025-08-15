CREATE DATABASE IF NOT EXISTS `docubrain_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `docubrain_db`;

-- ----------------------------
-- 1. 用户表 (user)
-- 存储应用的用户信息
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码 (实际应用中应存储哈希值)',
                        `email` VARCHAR(100) UNIQUE COMMENT '电子邮箱',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 2. 知识库表 (knowledge_base)
-- 存储用户创建的各个知识库的基本信息
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
                                  `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
                                  `description` VARCHAR(500) COMMENT '知识库描述',
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_user_id` (`user_id`) -- 为 user_id 创建索引以优化查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ----------------------------
-- 3. 文档信息表 (document_info)
-- 记录用户上传的每一个原始文档
-- ----------------------------
DROP TABLE IF EXISTS `document_info`;
CREATE TABLE `document_info` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
                                 `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
                                 `file_type` VARCHAR(20) COMMENT '文件类型 (e.g., pdf, txt)',
                                 `file_size` BIGINT COMMENT '文件大小 (字节)',
                                 `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态 (PENDING, INDEXING, COMPLETED, FAILED)',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_kb_id` (`knowledge_base_id`) -- 为 knowledge_base_id 创建索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档信息表';

-- ----------------------------
-- 4. 文档分块表 (document_chunk)
-- 存储文档被切分后的文本块，并与向量ID关联
-- ----------------------------
DROP TABLE IF EXISTS `document_chunk`;
CREATE TABLE `document_chunk` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `document_info_id` BIGINT NOT NULL COMMENT '所属文档ID',
                                  `vector_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '在向量数据库中的唯一ID',
                                  `chunk_text` TEXT NOT NULL COMMENT '文本块原文',
                                  `chunk_order` INT NOT NULL COMMENT '文本块在原文中的顺序',
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_doc_info_id` (`document_info_id`), -- 为 document_info_id 创建索引
                                  KEY `idx_vector_id` (`vector_id`) -- 为 vector_id 创建索引以便反向查找
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档分块表';


INSERT INTO `user` (`username`, `password`, `email`) VALUES
                                                         ('rexy', '123456', 'rexy@gmail.com'),
                                                         ('testuser', '123456', 'test@gmail.com');


-- user (1) -> (多) knowledge_base  : 一个用户可以有多个知识库
-- knowledge_base (1) -> (多) document_info : 一个知识库可以包含多个文档
-- document_info (1) -> (多) document_chunk : 一个文档可以被切分成多个文本块


COMMIT;