package com.yao.pharmacymall.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * 数据库初始化器 - 已禁用
 * 使用Spring Boot的spring.sql.init配置代替
 */
@Slf4j
// 使用 application-dev.yml 的 spring.sql.init，避免重复执行脚本
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化数据库...");
        try (Connection connection = dataSource.getConnection()) {
            // 检测数据库类型
            DatabaseMetaData metaData = connection.getMetaData();
            String dbName = metaData.getDatabaseProductName().toLowerCase();
            log.info("检测到数据库: {}", dbName);

            // 根据数据库类型选择脚本
            String scriptPath = dbName.contains("h2") ? "db/schema-h2.sql" : "db/schema.sql";
            log.info("使用初始化脚本: {}", scriptPath);

            // 执行初始化脚本
            ScriptUtils.executeSqlScript(connection,
                new ClassPathResource(scriptPath));
            log.info("数据库初始化完成！");
        } catch (Exception e) {
            log.warn("数据库初始化失败（可能已存在）: {}", e.getMessage());
        }
    }
}
