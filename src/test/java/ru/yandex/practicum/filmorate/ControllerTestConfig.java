package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@TestConfiguration
@Profile("test")  // Добавлена аннотация профиля
public class ControllerTestConfig {

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