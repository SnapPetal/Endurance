package com.thonbecker.endurance.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Testing database connection...");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("Database: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            log.info("Driver: {} {}", metaData.getDriverName(), metaData.getDriverVersion());
            log.info("URL: {}", connection.getMetaData().getURL());
            log.info("User: {}", connection.getMetaData().getUserName());

            // Test basic queries
            String currentSchema = jdbcTemplate.queryForObject("SELECT current_schema()", String.class);
            String currentUser = jdbcTemplate.queryForObject("SELECT current_user", String.class);
            String currentDatabase = jdbcTemplate.queryForObject("SELECT current_database()", String.class);

            log.info("Current Schema: {}", currentSchema);
            log.info("Current User: {}", currentUser);
            log.info("Current Database: {}", currentDatabase);

            // Test if we can create tables in public schema
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test_connection (id INT)");
                jdbcTemplate.execute("DROP TABLE test_connection");
                log.info("✓ Can create/drop tables in public schema");
            } catch (Exception e) {
                log.error("✗ Cannot create/drop tables in public schema: {}", e.getMessage());
            }

            // Test if endurance schema exists
            try {
                String schemaExists = jdbcTemplate.queryForObject(
                        "SELECT schema_name FROM information_schema.schemata WHERE schema_name = 'endurance'",
                        String.class);
                if (schemaExists != null) {
                    log.info("✓ Endurance schema exists");
                } else {
                    log.info("ℹ Endurance schema does not exist yet");
                }
            } catch (Exception e) {
                log.error("✗ Error checking endurance schema: {}", e.getMessage());
            }

        } catch (SQLException e) {
            log.error("✗ Database connection failed: {}", e.getMessage(), e);
            throw e;
        }

        log.info("Database connection test completed");
    }
}
