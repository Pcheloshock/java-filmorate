package ru.yandex.practicum.filmorate.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "jdbc", matchIfMissing = true)
    public FilmStorage filmDbStorage(JdbcTemplate jdbcTemplate) {
        return new FilmDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "memory")
    @Primary
    public FilmStorage inMemoryFilmStorage() {
        return new InMemoryFilmStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "jdbc", matchIfMissing = true)
    public UserStorage userDbStorage(JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "memory")
    @Primary
    public UserStorage inMemoryUserStorage() {
        return new InMemoryUserStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "jdbc", matchIfMissing = true)
    public GenreMpaStorage genreMpaDbStorage(JdbcTemplate jdbcTemplate) {
        return new GenreMpaDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "memory")
    @Primary
    public GenreMpaStorage inMemoryGenreMpaStorage() {
        return new InMemoryGenreMpaStorage();
    }
}