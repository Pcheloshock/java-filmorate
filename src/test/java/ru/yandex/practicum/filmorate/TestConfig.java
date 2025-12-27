package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.sql.DataSource;

@TestConfiguration
public class TestConfig {

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
    @Primary
    public FilmStorage filmStorage(JdbcTemplate jdbcTemplate) {
        return new FilmDbStorage(jdbcTemplate);
    }

    @Bean
    @Primary
    public UserStorage userStorage(JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }

    @Bean
    @Primary
    public GenreMpaStorage genreMpaStorage(JdbcTemplate jdbcTemplate) {
        return new GenreMpaDbStorage(jdbcTemplate);
    }
}