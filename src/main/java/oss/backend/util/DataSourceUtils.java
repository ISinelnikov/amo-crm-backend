package oss.backend.util;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceUtils {
    private DataSourceUtils() {
    }

    public static JdbcTemplate getJdbcTemplateByParams(String driverClassName, String databaseUrl,
                                                       String databaseUsername, String databasePassword) {
        return new JdbcTemplate(
                DataSourceUtils.createDataSource(driverClassName, databaseUrl,
                        databaseUsername, databasePassword)
        );
    }

    private static DataSource createDataSource(String driverClassName, String jdbcUrl, String jdbcUser, String jdbcPassword) {
        HikariConfig config = new HikariConfig();
        if (!StringUtils.hasText(jdbcUrl)) {
            throw new IllegalArgumentException("Can't create data source!");
        } else {
            config.setDriverClassName(driverClassName);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(jdbcUser);
            config.setPassword(jdbcPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(1);
            config.setAutoCommit(true);
        }
        // default tuning
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
