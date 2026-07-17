package edu.cit.mabini.meditrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.sql.DataSource;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RenderDatabaseConfig {

    /**
     * Builds the DataSource from Render's DATABASE_URL env var
     * (provided by the attached Postgres environment group).
     * This bean overrides Spring Boot's auto-configured DataSource.
     */
    @Bean
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        try {
            URI uri = new URI(databaseUrl.replace("postgresql://", "http://"));
            String userInfo = uri.getUserInfo();
            String username = userInfo == null ? null : userInfo.split(":")[0];
            String password = (userInfo == null || !userInfo.contains(":"))
                    ? null : userInfo.substring(userInfo.indexOf(":") + 1);

            String jdbcUrl = String.format(
                    "jdbc:postgresql://%s:%s%s",
                    uri.getHost(), uri.getPort(), uri.getPath());

            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Invalid DATABASE_URL: " + databaseUrl, ex);
        }
    }
}
