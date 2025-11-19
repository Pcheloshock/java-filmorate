package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.FilmValidator;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 1;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {
        // Используем FilmValidator для проверки
        FilmValidator.validateReleaseDate(film.getReleaseDate());

        if (film.getId() == 0) {
            film.setId(currentId++);
        }
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film filmUpdate) {
        log.info("Получен запрос на обновление фильма с ID: {}", filmUpdate.getId());

        // Проверяем существование фильма
        Film existingFilm = films.get(filmUpdate.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с ID " + filmUpdate.getId() + " не найден");
        }

        // Обновляем имя, если оно не null
        if (filmUpdate.getName() != null) {
            FilmValidator.validateName(filmUpdate.getName());
            existingFilm.setName(filmUpdate.getName());
        }

        // Обновляем описание, если оно не null
        if (filmUpdate.getDescription() != null) {
            FilmValidator.validateDescription(filmUpdate.getDescription());
            existingFilm.setDescription(filmUpdate.getDescription());
        }

        // Обновляем дату релиза, если она не null
        if (filmUpdate.getReleaseDate() != null) {
            FilmValidator.validateReleaseDate(filmUpdate.getReleaseDate());
            existingFilm.setReleaseDate(filmUpdate.getReleaseDate());
        }

        // Обновляем продолжительность, если она не равна 0 (значение по умолчанию для int)
        if (filmUpdate.getDuration() != 0) {//проверка не дает знак менять
            FilmValidator.validateDuration(filmUpdate.getDuration());
            existingFilm.setDuration(filmUpdate.getDuration());
        }

        films.put(existingFilm.getId(), existingFilm);
        log.info("Обновлен фильм: {}", existingFilm);
        return existingFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }
}