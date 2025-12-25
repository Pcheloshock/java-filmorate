package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.GenreMpaStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreMpaController {
    private final GenreMpaStorage genreMpaStorage;

    @GetMapping("/genres")
    public List<Genre> getAllGenres() {
        return genreMpaStorage.getAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return genreMpaStorage.getGenreById(id);
    }

    @GetMapping("/mpa")
    public List<MpaRating> getAllMpaRatings() {
        return genreMpaStorage.getAllMpaRatings();
    }

    @GetMapping("/mpa/{id}")
    public MpaRating getMpaRatingById(@PathVariable int id) {
        return genreMpaStorage.getMpaRatingById(id);
    }
}