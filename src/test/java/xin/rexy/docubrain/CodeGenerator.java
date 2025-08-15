package xin.rexy.docubrain;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * <p>
 * 用来根据数据库表结构，自动生成 Entity, Mapper, Service, Controller 等基础代码。
 * 只在开发阶段运行，不参与项目打包。
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // ==================== 1. 全局配置 ====================
        String projectPath = System.getProperty("user.dir"); // 获取当前项目路径
        String outputDir = projectPath + "/src/main/java";   // 设置生成代码的输出目录

        FastAutoGenerator.create("jdbc:mysql://localhost:3306/docubrain_db?useSSL=false&serverTimezone=Asia/Shanghai", "root", "230320")
                .globalConfig(builder -> {
                    builder.author("Rexy") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir(outputDir); // 指定输出目录
                })
                // ==================== 2. 包配置 ====================
                .packageConfig(builder -> {
                    builder.parent("xin.rexy.docubrain") // 设置父包名
                            .entity("entity")       // Entity 实体类包名
                            .mapper("mapper")       // Mapper 接口包名
                            .service("service")     // Service 接口包名
                            .serviceImpl("service.impl") // Service 实现类包名
                            .controller("controller") // Controller 控制器包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper")); // 设置 Mapper.xml 生成路径
                })
                // ==================== 3. 策略配置 ====================
                .strategyConfig(builder -> {
                    builder.addInclude("user", "knowledge_base", "document_info", "document_chunk") // 设置需要生成的表名
                            .addTablePrefix("") // 设置过滤表前缀
                            // --- Entity 策略配置 ---
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解
                            // --- Controller 策略配置 ---
                            .controllerBuilder()
                            .enableRestStyle() // 开启 @RestController 注解
                            // --- Service 策略配置 ---
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // Service 接口名格式: User -> UserService
                            .formatServiceImplFileName("%sServiceImpl") // Service 实现类名格式: User -> UserServiceImpl
                            // --- Mapper 策略配置 ---
                            .mapperBuilder()
                            .enableMapperAnnotation(); // 开启 @Mapper 注解
                })
                // ==================== 4. 模板引擎配置 ====================
                .templateEngine(new VelocityTemplateEngine()) // 使用 Velocity 引擎模板
                .execute();
    }
}