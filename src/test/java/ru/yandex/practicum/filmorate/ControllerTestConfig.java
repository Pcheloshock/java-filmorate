package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.filmorate.storage.film.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@TestConfiguration
public class ControllerTestConfig {

    @Bean
    public FilmStorage testFilmStorage() {  // Измените имя
        return new InMemoryFilmStorage();
    }

    @Bean
    public UserStorage testUserStorage() {  // Измените имя
        return new InMemoryUserStorage();
    }

    @Bean
    public GenreMpaStorage testGenreMpaStorage() {  // Измените имя
        return new InMemoryGenreMpaStorage();
    }
}