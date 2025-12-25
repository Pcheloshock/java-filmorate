package ru.yandex.practicum.filmorate.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @Primary
    public FilmStorage filmDbStorage(JdbcTemplate jdbcTemplate) {
        return new FilmDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(FilmStorage.class)
    public FilmStorage inMemoryFilmStorage() {
        return new InMemoryFilmStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "jdbc", matchIfMissing = true)
    @Primary
    public UserStorage userDbStorage(JdbcTemplate jdbcTemplate) {
        return new UserDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(UserStorage.class)
    public UserStorage inMemoryUserStorage() {
        return new InMemoryUserStorage();
    }

    @Bean
    @ConditionalOnProperty(name = "filmorate.storage.type", havingValue = "jdbc", matchIfMissing = true)
    @Primary
    public GenreMpaStorage genreMpaDbStorage(JdbcTemplate jdbcTemplate) {
        return new GenreMpaDbStorage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(GenreMpaStorage.class)
    public GenreMpaStorage inMemoryGenreMpaStorage() {
        return new InMemoryGenreMpaStorage();
    }
}