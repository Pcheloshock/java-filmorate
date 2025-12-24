package ru.yandex.practicum.filmorate;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.filmorate.storage.film.GenreMpaStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryGenreMpaStorage;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public GenreMpaStorage genreMpaStorage() {
        return new InMemoryGenreMpaStorage();
    }
}