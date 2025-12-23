package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@TestConfiguration
public class TestJdbcConfig {

    @Bean
    public FilmDbStorage filmDbStorage(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return new FilmDbStorage(jdbcTemplate);
    }

    @Bean
    public UserDbStorage userDbStorage(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }
}