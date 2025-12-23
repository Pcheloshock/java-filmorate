package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface GenreMpaStorage {
    List<Genre> getAllGenres();

    Genre getGenreById(int id);

    List<MpaRating> getAllMpaRatings();

    MpaRating getMpaRatingById(int id);
}