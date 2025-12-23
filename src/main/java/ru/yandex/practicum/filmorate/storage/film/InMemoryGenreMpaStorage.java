package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.List;

@Component
public class InMemoryGenreMpaStorage implements GenreMpaStorage {

    @Override
    public List<Genre> getAllGenres() {
        return List.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")
        );
    }

    @Override
    public Genre getGenreById(int id) {
        return getAllGenres().stream()
                .filter(genre -> genre.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Жанр не найден"));
    }

    @Override
    public List<MpaRating> getAllMpaRatings() {
        return List.of(
                new MpaRating(1, "G", "Нет возрастных ограничений"),
                new MpaRating(2, "PG", "Рекомендуется присутствие родителей"),
                new MpaRating(3, "PG-13", "Детям до 13 лет просмотр не желателен"),
                new MpaRating(4, "R", "Лицам до 17 лет обязательно присутствие взрослого"),
                new MpaRating(5, "NC-17", "Лицам до 18 лет просмотр запрещен")
        );
    }

    @Override
    public MpaRating getMpaRatingById(int id) {
        return getAllMpaRatings().stream()
                .filter(mpa -> mpa.getId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Рейтинг MPA не найден"));
    }
}