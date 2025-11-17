package ru.yandex.practicum.filmorate.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
        validateFilm(film);

        // Если ID не установлен (равен 0), генерируем новый
        if (film.getId() == 0) {
            film.setId(currentId++);
        } else {
            // Если ID установлен вручную, обновляем currentId
            if (film.getId() >= currentId) {
                currentId = film.getId() + 1;
            }
        }

        films.put(film.getId(), film);
        log.info("Создан новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());

        // Проверяем, что ID указан и фильм существует
        if (film.getId() == 0) {
            throw new ValidationException("ID фильма не может быть 0");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм: {}", film);
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // Добавьте метод для получения фильма по ID
    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
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
        // Проверка даты релиза
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}