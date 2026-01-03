package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@TestConfiguration
public class TestMemoryConfig {

    @Bean
    @Primary
    public FilmStorage inMemoryFilmStorage() {
        return new InMemoryFilmStorage();
    }

    @Bean
    @Primary
    public UserStorage inMemoryUserStorage() {
        return new InMemoryUserStorage();
    }

    @Bean
    @Primary
    public GenreMpaStorage inMemoryGenreMpaStorage() {
        return new InMemoryGenreMpaStorage();
    }
}