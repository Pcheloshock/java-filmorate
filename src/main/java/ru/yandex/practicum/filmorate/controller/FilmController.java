package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody Film film) {
        log.info("Создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getAllFilms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Пока возвращаем все фильмы, пагинацию добавим позже
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.findById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} удаляет лайк с фильма {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос {} популярных фильмов", count);

        // Для диагностики получаем ВСЕ фильмы
        List<Film> allFilms = filmService.findAll();
        log.info("Все фильмы в базе ({}):", allFilms.size());
        for (Film film : allFilms) {
            log.info("  Фильм ID={}, название='{}', лайков={}, рейтинг={}",
                    film.getId(), film.getName(),
                    film.getLikes() != null ? film.getLikes().size() : 0,
                    film.getRate() != null ? film.getRate() : 0);
        }

        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());

        return popularFilms;
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(required = false) List<Integer> genres,
            @RequestParam(required = false) Integer year) {
        log.info("Поиск фильмов: query={}, genres={}, year={}", query, genres, year);
        // Пока возвращаем пустой список, реализуем позже
        return List.of();
    }
}