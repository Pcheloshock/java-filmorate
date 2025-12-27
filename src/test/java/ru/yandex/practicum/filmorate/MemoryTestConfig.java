package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@TestConfiguration
public class MemoryTestConfig {

    @Bean
    @Primary  // Только в тестах
    public FilmStorage filmStorage() {
        return new InMemoryFilmStorage();
    }

    @Bean
    @Primary  // Только в тестах
    public UserStorage userStorage() {
        return new InMemoryUserStorage();
    }

    @Bean
    @Primary  // Только в тестах
    public GenreMpaStorage genreMpaStorage() {
        return new InMemoryGenreMpaStorage();
    }
}