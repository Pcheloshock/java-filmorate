package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryGenreMpaStorage implements GenreMpaStorage {
    private final Map<Integer, Genre> genres = new HashMap<>();
    private final Map<Integer, MpaRating> mpaRatings = new HashMap<>();

    public InMemoryGenreMpaStorage() {
        // Инициализируем жанры
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Мультфильм"));
        genres.put(4, new Genre(4, "Триллер"));
        genres.put(5, new Genre(5, "Документальный"));
        genres.put(6, new Genre(6, "Боевик"));

        // Инициализируем MPA рейтинги
        mpaRatings.put(1, new MpaRating(1, "G", "Нет возрастных ограничений"));
        mpaRatings.put(2, new MpaRating(2, "PG", "Рекомендуется присутствие родителей"));
        mpaRatings.put(3, new MpaRating(3, "PG-13", "Детям до 13 лет просмотр не желателен"));
        mpaRatings.put(4, new MpaRating(4, "R", "Лицам до 17 лет обязательно присутствие взрослого"));
        mpaRatings.put(5, new MpaRating(5, "NC-17", "Лицам до 18 лет просмотр запрещен"));
    }

    @Override
    public List<Genre> getAllGenres() {
        return new ArrayList<>(genres.values());
    }

    @Override
    public Genre getGenreById(int id) {
        return genres.get(id);
    }

    @Override
    public List<MpaRating> getAllMpaRatings() {
        return new ArrayList<>(mpaRatings.values());
    }

    @Override
    public MpaRating getMpaRatingById(int id) {
        return mpaRatings.get(id);
    }
}