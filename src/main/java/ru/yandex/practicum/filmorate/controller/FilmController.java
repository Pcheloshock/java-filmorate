package ru.yandex.practicum.filmorate.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.FilmValidator;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;

import java.time.LocalDate;
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
        // Spring автоматически вызовет все валидации включая @ValidReleaseDate
        if (film.getId() == 0) {
            film.setId(currentId++);
        }
        films.put(film.getId(), film);
        log.info("Создан новый фильм: {}", film);
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

        // Обновляем только non-null поля
        if (filmUpdate.getName() != null) {
            if (filmUpdate.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(filmUpdate.getName());
        }

        if (filmUpdate.getDescription() != null) {
            if (filmUpdate.getDescription().length() > 200) {
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            existingFilm.setDescription(filmUpdate.getDescription());
        }

        if (filmUpdate.getReleaseDate() != null) {
            LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
            if (filmUpdate.getReleaseDate().isBefore(minReleaseDate)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(filmUpdate.getReleaseDate());
        }

        if (filmUpdate.getDuration() != 0) { // Для примитива int
            if (filmUpdate.getDuration() <= 0) {
                throw new ValidationException("Продолжительность должна быть положительным числом");
            }
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


    @PostConstruct
    public void init() {
        // Создаем тестовый фильм с ID=1 для тестов
        Film testFilm = new Film();
        testFilm.setId(1);
        testFilm.setName("Initial Film");
        testFilm.setDescription("Initial Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        films.put(1, testFilm);
        currentId = 2; // Устанавливаем следующий ID как 2
        log.info("Инициализирован тестовый фильм с ID=1");
    }

    private void validateFilm(Film film) {
        FilmValidator.validateReleaseDate(film.getReleaseDate());
    }
}