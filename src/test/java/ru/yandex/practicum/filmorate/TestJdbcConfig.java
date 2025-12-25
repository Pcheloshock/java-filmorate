package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.sql.DataSource;

@TestConfiguration
public class TestJdbcConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("data.sql")
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public FilmStorage filmDbStorage(JdbcTemplate jdbcTemplate) {
        return new FilmDbStorage(jdbcTemplate);
    }

    @Bean
    public UserStorage userDbStorage(JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }

    @Bean
    public GenreMpaStorage genreMpaDbStorage(JdbcTemplate jdbcTemplate) {
        return new GenreMpaDbStorage(jdbcTemplate);
    }
}